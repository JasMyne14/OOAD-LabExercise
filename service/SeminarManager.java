import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Central Service Class for Seminar Management
// Handles Users, Presentations, Sessions, and Persistence

public class SeminarManager {
    // In-memory database
    private List<User> users;
    private List<Presentation> presentations;
    private List<SeminarSession> sessions;
    private final String DATA_FILE = "seminar_data.ser"; // The file where data is stored

    public SeminarManager() {
        users = new ArrayList<>();
        presentations = new ArrayList<>();
        sessions = new ArrayList<>();
        loadData(); // Load existing data from file
        if(users.isEmpty()) seedData(); // Create some default data if none exists
    }

    // --- Core Authentication Logic ---
    public User login(String id, String role) {
        // Simple authentication based on ID and role
        return users.stream()
            .filter(u -> u.getId().equals(id) && u.getClass().getSimpleName().equals(role))
            .findFirst().orElse(null);
    }

    // --- Presentation Management ---
    public void registerPresentation(Presentation p) {
        // Remove existing presentation by the same student (if any)
        presentations.removeIf(exist -> exist.getStudentId().equals(p.getStudentId()));
        presentations.add(p);
        saveData(); // Commit changes
    }

    // --- Seminar Session Management ---
    public void createSession(SeminarSession s) {
        sessions.add(s);
        saveData();
    }
    
    // Delete Session
    public void deleteSession(SeminarSession s) {
        sessions.remove(s);
        saveData();
    }
    
    // Update People's Choice Votes
    public void updateVotes(String studentId, int votes) {
        Presentation p = getPresentationByStudent(studentId);
        if(p != null) {
            p.setAudienceVotes(votes);
            saveData();
        }
    }

    // Assign Evaluators and Students to Session
    // If Poster Session, it automatically assign Board IDs
    public void assignToSession(SeminarSession session, List<String> evalIds, List<String> studIds) {
        // Update the session's evaluator and student lists
        session.getEvaluatorIds().clear();
        session.getEvaluatorIds().addAll(evalIds);
        
        session.getStudentIds().clear();
        session.getStudentIds().addAll(studIds);
        
        // Assign Board IDs if Poster Session
        if(session.getType().equals("Poster")) {
            int count = 1;
            for(String sId : studIds) {
                Presentation p = getPresentationByStudent(sId);
                // Assign Board ID like "B-01", "B-02", ...
                if(p != null) p.setBoardId("B-" + String.format("%02d", count++));
            }
        }
        saveData();
    }

    // --- Evaluation Logic ---
    public void addEvaluation(String studentId, Evaluation e) {
        Presentation p = getPresentationByStudent(studentId);
        if (p != null) {
            // Remove existing evaluation by the same evaluator (update score)
            p.getEvaluations().removeIf(old -> old.getEvaluatorId().equals(e.getEvaluatorId()));
            p.addEvaluation(e);
            saveData();
        }
    }

    // ---- User Management Logic ---
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

    // --- Data Access Helpers ---
    public List<Presentation> getAllPresentations() { return presentations; }
    public List<SeminarSession> getAllSessions() { return sessions; }
    
    public List<User> getUsersByRole(Class<?> role) {
        return users.stream().filter(u -> role.isInstance(u)).collect(Collectors.toList());
    }
    public Presentation getPresentationByStudent(String sId) {
        return presentations.stream().filter(p -> p.getStudentId().equals(sId)).findFirst().orElse(null);
    }

    public Presentation getPresentationByStudentName(String name) {
        return presentations.stream()
            .filter(p -> p.getStudentName().equals(name))
            .findFirst().orElse(null);
    }

    // --- Persistence (Save/Load)---
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
        // Default Users for Testing
        users.add(new Coordinator("C001", "Dr. Ng Hu", "pass"));
        users.add(new Student("S001", "Jasmyne Yap", "pass"));
        users.add(new Student("S002", "Wan Hanani", "pass"));
        users.add(new Evaluator("E001", "Prof. Josh", "pass"));
        users.add(new Evaluator("E002", "Dr. Lim", "pass"));
        saveData();
    }
}