import java.awt.*;
import javax.swing.*; 

// Main Application Class (Card Layout for Login and Dashboards)
public class MainApp extends JFrame {
    private SeminarManager manager;    // Central service for data handling
    private JPanel mainPanel;          // The container for all screens (Login vs Dashboard)
    private CardLayout cardLayout;     // CardLayout to switch between views

    public MainApp() {
        // 1. Initialize the Data Manager (Loads data from file immediately)
        manager = new SeminarManager(); 

        // 2. Setup the Main Window
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 3. Setup Screens
        setupLogin();   // Enhanced Login Screen
        
        // 4. Final Window Configuration
        add(mainPanel);
        setTitle("FCI Post Graduate Academic Seminar System");
        setSize(900, 700); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }

    // --- ENHANCED LOGIN SCREEN ---
    private void setupLogin() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE); // Clean white background for login
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2; // Allow headers to span across columns

        // 1. SYSTEM NAME (Title)
        JLabel titleLbl = new JLabel("FCI Post Graduate Academic Seminar System", SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLbl.setForeground(new Color(40, 60, 100)); // Professional Dark Blue
        
        // 2. DESCRIPTION
        JLabel descLbl = new JLabel("<html><center>A comprehensive platform for scheduling seminars,<br>"
                + "managing presentations, and conducting evaluations.</center></html>", SwingConstants.CENTER);
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLbl.setForeground(Color.DARK_GRAY);
        
        // 3. INSTRUCTIONS
        JLabel instrLbl = new JLabel("Please enter your User ID and select your Role to log in:", SwingConstants.CENTER);
        instrLbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
        instrLbl.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Spacing

        // --- INPUT FIELDS ---
        JTextField userField = new JTextField(15);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Evaluator", "Coordinator"});
        JButton loginBtn = new JButton("Login to System");
        
        // Style the login button
        loginBtn.setBackground(new Color(70, 130, 180)); // Steel Blue
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginBtn.setPreferredSize(new Dimension(150, 35));

        // --- ADDING COMPONENTS TO LAYOUT ---
        
        // Header Section
        gbc.gridx=0; gbc.gridy=0; loginPanel.add(titleLbl, gbc);
        gbc.gridx=0; gbc.gridy=1; loginPanel.add(descLbl, gbc);
        gbc.gridx=0; gbc.gridy=2; loginPanel.add(instrLbl, gbc);
        gbc.gridx=0; gbc.gridy=3; loginPanel.add(new JSeparator(), gbc); // Line divider

        // Input Section (Reset gridwidth to 1)
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        // User ID Row
        gbc.gridx=0; gbc.gridy=4; 
        JLabel idLabel = new JLabel("User ID:", SwingConstants.RIGHT);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        loginPanel.add(idLabel, gbc);
        gbc.gridx=1; loginPanel.add(userField, gbc);
        
        // Role Row
        gbc.gridx=0; gbc.gridy=5; 
        JLabel roleLabel = new JLabel("Role:", SwingConstants.RIGHT);
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        loginPanel.add(roleLabel, gbc);
        gbc.gridx=1; loginPanel.add(roleBox, gbc);
        
        // Button Row (Centered)
        gbc.gridwidth = 2;
        gbc.gridx=0; gbc.gridy=6; 
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginBtn, gbc);

        // --- LOGIN LOGIC ---
        loginBtn.addActionListener(e -> {
            try {
                String role = (String)roleBox.getSelectedItem();
                String id = userField.getText().trim();

                // Authenticate User
                User u = manager.login(id, role);
                
                if(u != null) {
                    // Successful Login: Create the Dashboard View based on role
                    JPanel specificView = null;
                    if(u instanceof Coordinator) {
                        specificView = new CoordinatorView(manager);
                    } else if(u instanceof Evaluator) {
                        specificView = new EvaluatorView(manager, (Evaluator)u);
                    } else if(u instanceof Student) {
                        specificView = new StudentView(manager, (Student)u);
                    }

                    if(specificView != null) {
                        // Top Bar with Logout
                        JPanel dashboardWrapper = new JPanel(new BorderLayout());
                        
                        // Create Top Bar (USING YOUR PINK COLOR)
                        JPanel topBar = new JPanel(new BorderLayout());
                        topBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                        topBar.setBackground(new Color(244, 194, 194)); // Pink
                        
                        JLabel welcomeLbl = new JLabel("Welcome, " + u.getUsername() + " (" + role + ")");
                        welcomeLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                        
                        JButton logoutBtn = new JButton("Logout");
                        logoutBtn.setFocusable(false);
                        logoutBtn.addActionListener(evt -> logout()); // Call logout method

                        topBar.add(welcomeLbl, BorderLayout.WEST);
                        topBar.add(logoutBtn, BorderLayout.EAST);

                        // Assemble Dashboard
                        dashboardWrapper.add(topBar, BorderLayout.NORTH);
                        dashboardWrapper.add(specificView, BorderLayout.CENTER);

                        // Show Dashboard
                        mainPanel.add(dashboardWrapper, "DASH");
                        cardLayout.show(mainPanel, "DASH");
                        
                        userField.setText(""); // Clear password field for security 
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Login Failed.\nUser ID not found for role: " + role);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        mainPanel.add(loginPanel, "LOGIN");
    }

    // --- LOGOUT LOGIC ---
    public void logout() {
        // Switch back to login screen
        cardLayout.show(mainPanel, "LOGIN");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}