import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Presentation Class
public class Presentation implements Serializable {
    private String studentId;
    private String studentName;
    private String title;
    private String abstractText;
    private String supervisor;
    private String type;            // "Oral" or "Poster"
    private String filePath;        // Path to uploaded file
    private String boardId;         // Assigned Evaluation Board ID
    private int audienceVotes;      // People's Choice Votes
    
    // List of Evaluations received from different Evaluators
    private List<Evaluation> evaluations;

    public Presentation(String sId, String sName, String title, String abs, String sup, String type, String path) {
        this.studentId = sId;
        this.studentName = sName;
        this.title = title;
        this.abstractText = abs;
        this.supervisor = sup;
        this.type = type;
        this.filePath = path;
        this.evaluations = new ArrayList<>();
        this.audienceVotes = 0;
    }

    // Management Methods
    public void addEvaluation(Evaluation e) { evaluations.add(e); }
    public List<Evaluation> getEvaluations() { return evaluations; }
    
    // Getters and Setters
    public String getStudentId() { return studentId; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getStudentName() { return studentName; }
    
    // Getter for Evaluation Details
    public String getAbstractText() { return abstractText; }
    public String getSupervisor() { return supervisor; }
    public String getFilePath() { return filePath; }

    // Poster Management 
    public void setBoardId(String id) { this.boardId = id; }
    public String getBoardId() { return boardId; }
    // Awards Management
    public void setAudienceVotes(int votes) { this.audienceVotes = votes; }
    public int getAudienceVotes() { return audienceVotes; }
    // Grade Check
    public boolean isGradedBy(String evalId) {
        return evaluations.stream().anyMatch(e -> e.getEvaluatorId().equals(evalId));
    }
    
    @Override
    public String toString() { return title + " (" + studentName + ")"; }
}