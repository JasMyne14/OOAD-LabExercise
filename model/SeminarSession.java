import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Seminar Session Class
public class SeminarSession implements Serializable {
    private String sessionId;      // Unique ID
    private String date;           // Format: DD/MM/YYYY
    private String time;           // Format: HH:MM - HH:MM
    private String venue;          // Venue Name
    private String type;           // "Oral" or "Poster"
    
    private List<String> assignedEvaluatorIds;
    private List<String> assignedStudentIds;

    public SeminarSession(String id, String date, String time, String venue, String type) { 
        this.sessionId = id;
        this.date = date;
        this.time = time; 
        this.venue = venue;
        this.type = type;
        this.assignedEvaluatorIds = new ArrayList<>();
        this.assignedStudentIds = new ArrayList<>();
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public String getDate() { return date; }
    public String getTime() { return time; } 
    public String getVenue() { return venue; }
    public String getType() { return type; }
    
    // Returns the lists of IDs so the CoordinatorView can modify them
    public List<String> getEvaluatorIds() { return assignedEvaluatorIds; }
    public List<String> getStudentIds() { return assignedStudentIds; }
    
    @Override
    public String toString() { return date + " (" + time + ") - " + venue + " [" + type + "]"; } // <--- UPDATE TOSTRING
}