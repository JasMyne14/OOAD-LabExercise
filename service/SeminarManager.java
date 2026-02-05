import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeminarManager {
    private List<User> users;
    private List<Presentation> presentations;
    private List<SeminarSession> sessions;
    private final String DATA_FILE = "seminar_data.ser";

    public SeminarManager() {
        users = new ArrayList<>();
        presentations = new ArrayList<>();
        sessions = new ArrayList<>();
        loadData();
        if(users.isEmpty()) seedData(); 
    }

    // --- Core Logic ---
    public User login(String id, String role) {
        return users.stream()
            .filter(u -> u.getId().equals(id) && u.getClass().getSimpleName().equals(role))
            .findFirst().orElse(null);
    }

    public void registerPresentation(Presentation p) {
        presentations.removeIf(exist -> exist.getStudentId().equals(p.getStudentId()));
        presentations.add(p);
        saveData();
    }

    public void createSession(SeminarSession s) {
        sessions.add(s);
        saveData();
    }
    
    // NEW: Delete Session
    public void deleteSession(SeminarSession s) {
        sessions.remove(s);
        saveData();
    }
    
    // NEW: Update Votes
    public void updateVotes(String studentId, int votes) {
        Presentation p = getPresentationByStudent(studentId);
        if(p != null) {
            p.setAudienceVotes(votes);
            saveData();
        }
    }

    public void assignToSession(SeminarSession session, List<String> evalIds, List<String> studIds) {
        session.getEvaluatorIds().clear();
        session.getEvaluatorIds().addAll(evalIds);
        
        session.getStudentIds().clear();
        session.getStudentIds().addAll(studIds);
        
        if(session.getType().equals("Poster")) {
            int count = 1;
            for(String sId : studIds) {
                Presentation p = getPresentationByStudent(sId);
                if(p != null) p.setBoardId("B-" + String.format("%02d", count++));
            }
        }
        saveData();
    }

    public void addEvaluation(String studentId, Evaluation e) {
        Presentation p = getPresentationByStudent(studentId);
        if (p != null) {
            p.getEvaluations().removeIf(old -> old.getEvaluatorId().equals(e.getEvaluatorId()));
            p.addEvaluation(e);
            saveData();
        }
    }

    // --- NEW: User Management Logic ---
    public void addUser(User newUser) {
        // Check if ID already exists to prevent duplicates
        if(users.stream().anyMatch(u -> u.getId().equalsIgnoreCase(newUser.getId()))) {
            throw new IllegalArgumentException("User ID " + newUser.getId() + " already exists!");
        }
        users.add(newUser);
        saveData(); // Save to file immediately
    }

    public void deleteUser(String userId) {
        users.removeIf(u -> u.getId().equals(userId));
        saveData();
    }

    public Presentation getPresentationByStudentName(String name) {
        return presentations.stream()
            .filter(p -> p.getStudentName().equals(name))
            .findFirst().orElse(null);
    }
    
    // --- Data Access Helpers ---
    public List<Presentation> getAllPresentations() { return presentations; }
    public List<SeminarSession> getAllSessions() { return sessions; }
    public List<User> getUsersByRole(Class<?> role) {
        return users.stream().filter(u -> role.isInstance(u)).collect(Collectors.toList());
    }
    public Presentation getPresentationByStudent(String sId) {
        return presentations.stream().filter(p -> p.getStudentId().equals(sId)).findFirst().orElse(null);
    }

    // --- Persistence ---
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
            oos.writeObject(presentations);
            oos.writeObject(sessions);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            users = (List<User>) ois.readObject();
            presentations = (List<Presentation>) ois.readObject();
            sessions = (List<SeminarSession>) ois.readObject(); 
        } catch (Exception e) { 
            System.out.println("No previous data found or error loading."); 
        }
    }

    private void seedData() {
        users.add(new Coordinator("C001", "Dr. Ng Hu", "pass"));
        users.add(new Student("S001", "Jasmyne Yap", "pass"));
        users.add(new Student("S002", "Wan Hanani", "pass"));
        users.add(new Evaluator("E001", "Prof. Josh", "pass"));
        users.add(new Evaluator("E002", "Dr. Lim", "pass"));
        saveData();
    }
}