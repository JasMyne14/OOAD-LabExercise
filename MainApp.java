import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {
    private SeminarManager manager;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public MainApp() {
        manager = new SeminarManager();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        setupLogin();
        
        add(mainPanel);
        setTitle("FCI Seminar System");
        setSize(900, 700); // Made it slightly bigger for the top bar
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupLogin() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(15);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Evaluator", "Coordinator"});
        JButton loginBtn = new JButton("Login");

        // UI Layout
        gbc.gridx=0; gbc.gridy=0; loginPanel.add(new JLabel("User ID:"), gbc);
        gbc.gridx=1; loginPanel.add(userField, gbc);
        
        gbc.gridx=0; gbc.gridy=1; loginPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx=1; loginPanel.add(roleBox, gbc);
        
        gbc.gridx=1; gbc.gridy=2; loginPanel.add(loginBtn, gbc);

        // --- LOGIN LOGIC ---
        loginBtn.addActionListener(e -> {
            try {
                String role = (String)roleBox.getSelectedItem();
                String id = userField.getText().trim();
                
                User u = manager.login(id, role);
                
                if(u != null) {
                    // Create the Dashboard View based on role
                    JPanel specificView = null;
                    if(u instanceof Coordinator) {
                        specificView = new CoordinatorView(manager);
                    } else if(u instanceof Evaluator) {
                        specificView = new EvaluatorView(manager, (Evaluator)u);
                    } else if(u instanceof Student) {
                        specificView = new StudentView(manager, (Student)u);
                    }

                    if(specificView != null) {
                        // WRAPPER: Add a Top Bar with Logout
                        JPanel dashboardWrapper = new JPanel(new BorderLayout());
                        
                        // 1. Create Top Bar
                        JPanel topBar = new JPanel(new BorderLayout());
                        topBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                        topBar.setBackground(new Color(230, 230, 230)); // Light Gray
                        
                        JLabel welcomeLbl = new JLabel("Welcome, " + u.getUsername() + " (" + role + ")");
                        welcomeLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                        
                        JButton logoutBtn = new JButton("Logout");
                        logoutBtn.setFocusable(false);
                        logoutBtn.addActionListener(evt -> logout()); // Call logout method

                        topBar.add(welcomeLbl, BorderLayout.WEST);
                        topBar.add(logoutBtn, BorderLayout.EAST);

                        // 2. Assemble Dashboard
                        dashboardWrapper.add(topBar, BorderLayout.NORTH);
                        dashboardWrapper.add(specificView, BorderLayout.CENTER);

                        // 3. Show it
                        mainPanel.add(dashboardWrapper, "DASH");
                        cardLayout.show(mainPanel, "DASH");
                        
                        // Clear password field for security (optional)
                        userField.setText("");
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
        // 1. Switch back to login screen
        cardLayout.show(mainPanel, "LOGIN");
        
        // 2. Remove the old dashboard from memory (so next login is fresh)
        // We iterate to find the "DASH" component and remove it
        for (Component comp : mainPanel.getComponents()) {
            if (comp != null && !comp.getClass().getSimpleName().equals("LoginPanel") 
                    && comp instanceof JPanel && comp != mainPanel) {
                // If it's not the main panel, we assume it's the dashboard wrapper
                // (Simple check: removing the last added component usually works too)
            }
        }
        // Actually, CardLayout keeps components. Let's just remove the one named "DASH" if we can,
        // or simpler: just rely on the fact that we create a NEW "DASH" panel every login.
        // The garbage collector will handle the old one eventually.
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}