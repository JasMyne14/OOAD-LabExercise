import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Presentation implements Serializable {
    private String studentId;
    private String studentName;
    private String title;
    private String abstractText;
    private String supervisor;
    private String type; // "Oral" or "Poster"
    private String filePath;
    private String boardId; 
    private int audienceVotes; 
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

    public void addEvaluation(Evaluation e) { evaluations.add(e); }
    public List<Evaluation> getEvaluations() { return evaluations; }
    public String getStudentId() { return studentId; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getStudentName() { return studentName; }
    
    public void setBoardId(String id) { this.boardId = id; }
    public String getBoardId() { return boardId; }
    
    // Allow setting votes manually for the Coordinator
    public void setAudienceVotes(int votes) { this.audienceVotes = votes; }
    public int getAudienceVotes() { return audienceVotes; }
    
    public boolean isGradedBy(String evalId) {
        return evaluations.stream().anyMatch(e -> e.getEvaluatorId().equals(evalId));
    }
    
    @Override
    public String toString() { return title + " (" + studentName + ")"; }
}