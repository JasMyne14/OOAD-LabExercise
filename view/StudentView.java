import javax.swing.*;
import java.awt.*;

// Student Dashboard View
public class StudentView extends JPanel {
    public StudentView(SeminarManager manager, Student student) {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;
        
        // Form Components
        JTextField title = new JTextField(20);
        JTextArea abs = new JTextArea(5, 20);
        JTextField sup = new JTextField(15);
        JComboBox<String> type = new JComboBox<>(new String[]{"Oral", "Poster"});
        JButton upload = new JButton("Upload File (Select)");
        JButton register = new JButton("Register");
        
        // To store selected file path
        final String[] filePath = {""}; 

        upload.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            // Show open file dialog
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePath[0] = fc.getSelectedFile().getAbsolutePath();
                upload.setText("File Selected");
            }
        });

        // Register Button Logic
        register.addActionListener(e -> {
            // Validation: Ensure mandatory fields are filled
            if(title.getText().isEmpty() || sup.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            // Create Presentation Object and register
            Presentation p = new Presentation(
                student.getId(), 
                student.getUsername(),
                title.getText(), 
                abs.getText(), 
                sup.getText(),
                (String)type.getSelectedItem(),
                 filePath[0]
            );
            // Save to Manager
            manager.registerPresentation(p);
            JOptionPane.showMessageDialog(this, "Registration Successful!");
        });

        // Layout the components
        g.gridx=0; g.gridy=0; add(new JLabel("Title:"), g); g.gridx=1; add(title, g);
        g.gridx=0; g.gridy=1; add(new JLabel("Abstract:"), g); g.gridx=1; add(new JScrollPane(abs), g);
        g.gridx=0; g.gridy=2; add(new JLabel("Supervisor:"), g); g.gridx=1; add(sup, g);
        g.gridx=0; g.gridy=3; add(new JLabel("Type:"), g); g.gridx=1; add(type, g);
        g.gridx=1; g.gridy=4; add(upload, g);
        g.gridx=1; g.gridy=5; add(register, g);
    }
}