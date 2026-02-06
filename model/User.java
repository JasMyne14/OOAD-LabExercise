import java.io.Serializable;

// Abstract User Class
public abstract class User implements Serializable {
    private String id;          // Unique ID
    private String username;    // Username
    private String password;    // Simple placeholder

    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    
    @Override 
    public String toString() { return username + " (" + id + ")"; }
}
