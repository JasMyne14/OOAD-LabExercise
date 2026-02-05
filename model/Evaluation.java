import java.io.Serializable;

public class Evaluation implements Serializable {
    private String evaluatorId;
    private int problemClarity; // Score 1
    private int methodology;    // Score 2
    private int results;        // Score 3
    private int presentation;   // Score 4
    private String comments;

    public Evaluation(String evaluatorId, int p, int m, int r, int pre, String comments) {
        this.evaluatorId = evaluatorId;
        this.problemClarity = p;
        this.methodology = m;
        this.results = r;
        this.presentation = pre;
        this.comments = comments;
    }

    public int getTotal() { return problemClarity + methodology + results + presentation; }
    
    public String getEvaluatorId() { return evaluatorId; }
    public String getComments() { return comments; }

    //  GETTERS 
    public int getScore1() { return problemClarity; }
    public int getScore2() { return methodology; }
    public int getScore3() { return results; }
    public int getScore4() { return presentation; }
}