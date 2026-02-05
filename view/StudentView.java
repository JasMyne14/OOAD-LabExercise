import javax.swing.*;
import java.awt.*;

public class StudentView extends JPanel {
    public StudentView(SeminarManager manager, Student student) {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField title = new JTextField(20);
        JTextArea abs = new JTextArea(5, 20);
        JTextField sup = new JTextField(15);
        JComboBox<String> type = new JComboBox<>(new String[]{"Oral", "Poster"});
        JButton upload = new JButton("Upload File (Select)");
        JButton register = new JButton("Register");
        
        final String[] filePath = {""}; // Hack to store inside lambda

        upload.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePath[0] = fc.getSelectedFile().getAbsolutePath();
                upload.setText("File Selected");
            }
        });

        register.addActionListener(e -> {
            Presentation p = new Presentation(
                student.getId(), student.getUsername(),
                title.getText(), abs.getText(), sup.getText(),
                (String)type.getSelectedItem(), filePath[0]
            );
            manager.registerPresentation(p);
            JOptionPane.showMessageDialog(this, "Registration Successful!");
        });

        g.gridx=0; g.gridy=0; add(new JLabel("Title:"), g); g.gridx=1; add(title, g);
        g.gridx=0; g.gridy=1; add(new JLabel("Abstract:"), g); g.gridx=1; add(new JScrollPane(abs), g);
        g.gridx=0; g.gridy=2; add(new JLabel("Supervisor:"), g); g.gridx=1; add(sup, g);
        g.gridx=0; g.gridy=3; add(new JLabel("Type:"), g); g.gridx=1; add(type, g);
        g.gridx=1; g.gridy=4; add(upload, g);
        g.gridx=1; g.gridy=5; add(register, g);
    }
}