import javax.swing.*;
import java.awt.*;
import java.util.List;


// Evaluator Dashboard View
public class EvaluatorView extends JPanel {
    
    // Declare components as class fields so we can access them in methods
    private JSlider clarity, method, result, pres;
    private JTextField comments;
    private JButton submitBtn, editBtn;
    private JList<Presentation> pList;
    
    // Data references
    private SeminarManager manager;
    private Evaluator evaluator; // Logged-in evaluator

    public EvaluatorView(SeminarManager manager, Evaluator evaluator) {
        this.manager = manager;
        this.evaluator = evaluator;
        
        setLayout(new BorderLayout(10, 10)); 
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- LEFT SIDE: LIST of Assigned Proposal ---
        DefaultListModel<Presentation> listModel = new DefaultListModel<>();
        for(SeminarSession s : manager.getAllSessions()) {
            if(s.getEvaluatorIds().contains(evaluator.getId())) {
                // If assigned to this session, load all its students
                for(String sId : s.getStudentIds()) {
                    Presentation p = manager.getPresentationByStudent(sId);
                    if(p != null) listModel.addElement(p);
                }
            }
        }
        
        pList = new JList<>(listModel);
        pList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Changes text color based on grading status
        pList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Presentation p = (Presentation)value;
                setText(p.getTitle() + " (" + p.getStudentName() + ")");
                
                // If already graded, set text color to green
                if(p.isGradedBy(evaluator.getId())) {
                    setForeground(new Color(0, 128, 0)); 
                    if(isSelected) setForeground(Color.WHITE);
                }
                return this;
            }
        });

        JScrollPane listScroll = new JScrollPane(pList);
        listScroll.setPreferredSize(new Dimension(250, 0));
        listScroll.setBorder(BorderFactory.createTitledBorder("Assigned to Me"));

        // --- RIGHT SIDE: EVALUATION FORM ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Evaluation Rubric"));

        clarity = createStyledSlider();
        method = createStyledSlider();
        result = createStyledSlider();
        pres = createStyledSlider();
        comments = new JTextField();
        
        submitBtn = new JButton("Save Evaluation");
        editBtn = new JButton("Edit Score");
        editBtn.setEnabled(false); // Disabled by default

        // Add sliders to form panel
        addSliderToPanel(formPanel, "a. Problem & Clarity:", clarity);
        addSliderToPanel(formPanel, "b. Methodology & Approach:", method);
        addSliderToPanel(formPanel, "c. Findings & Results:", result);
        addSliderToPanel(formPanel, "d. Presentation & Q&A:", pres);
        
        // Comments Panel
        JPanel commPanel = new JPanel(new BorderLayout());
        commPanel.setMaximumSize(new Dimension(1000, 60));
        commPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        commPanel.add(new JLabel("Comments: "), BorderLayout.WEST);
        commPanel.add(comments, BorderLayout.CENTER);
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setMaximumSize(new Dimension(1000, 50));
        btnPanel.add(editBtn);
        btnPanel.add(submitBtn);
        
        formPanel.add(commPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(btnPanel);

        // --- LISTENERS ---

        // 1. LIST SELECTION: Load data & determine state
        pList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadPresentationData(pList.getSelectedValue());
            }
        });

        // 2. EDIT BUTTON: Unlock the form
        editBtn.addActionListener(e -> {
            setFormEnabled(true);    // Enable sliders
            submitBtn.setEnabled(true);    // Enable save button
            editBtn.setEnabled(false);     // Cannot edit while editing
        });

        // 3. SUBMIT BUTTON: Save and Lock
        submitBtn.addActionListener(e -> {
            Presentation selected = pList.getSelectedValue();
            if(selected == null) return;
            
            // Create Evaluation Object
            Evaluation eval = new Evaluation(
                evaluator.getId(),
                clarity.getValue(),
                method.getValue(),
                result.getValue(),
                pres.getValue(),
                comments.getText()
            );
            
            // Save Evaluation to Presentation
            manager.addEvaluation(selected.getStudentId(), eval);
            JOptionPane.showMessageDialog(this, "Score Saved: " + eval.getTotal() + "/20");
            
            // Visual Updates
            repaint();                      // Update green color in list
            setFormEnabled(false); // Lock form again
            editBtn.setEnabled(true);    // Allow editing
            submitBtn.setEnabled(false); // Disable save until edit is clicked
        });

        // Initial State: Disable everything until selection
        setFormEnabled(false);
        submitBtn.setEnabled(false);

        // Add components to main panel
        add(listScroll, BorderLayout.WEST);
        add(formPanel, BorderLayout.CENTER);
    }

    // --- LOGIC: Load Existing Data ---
    // Determine if the form should be in read-only or editable state
    private void loadPresentationData(Presentation p) {
        if (p == null) return;

        // Find if I have already graded this student
        Evaluation existing = null;
        for (Evaluation ev : p.getEvaluations()) {
            if (ev.getEvaluatorId().equals(evaluator.getId())) {
                existing = ev;
                break;
            }
        }

        if (existing != null) {
            // CASE 1: ALREADY GRADED -> Show Data, Lock Form
            clarity.setValue(existing.getScore1());
            method.setValue(existing.getScore2());
            result.setValue(existing.getScore3());
            pres.setValue(existing.getScore4());
            comments.setText(existing.getComments());

            setFormEnabled(false); // Read-only
            editBtn.setEnabled(true); // Allow user to unlock
            submitBtn.setEnabled(false); // Cannot save unless edit is clicked
        } else {
            // CASE 2: NEW -> Reset Form, Unlock Form
            clarity.setValue(3);
            method.setValue(3);
            result.setValue(3);
            pres.setValue(3);
            comments.setText("");

            setFormEnabled(true); // Editable
            editBtn.setEnabled(false); // Nothing to edit
            submitBtn.setEnabled(true); // Ready to save
        }
    }

    // --- HELPER: Lock/Unlock Form ---
    private void setFormEnabled(boolean enabled) {
        clarity.setEnabled(enabled);
        method.setEnabled(enabled);
        result.setEnabled(enabled);
        pres.setEnabled(enabled);
        comments.setEditable(enabled);
    }

    // --- VISUAL HELPERS ---
    private JSlider createStyledSlider() {
        JSlider slider = new JSlider(1, 5, 3); // Min 1, Max 5, Initial 3
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true); 
        slider.setFont(new Font("SansSerif", Font.BOLD, 12));
        return slider;
    }

    private void addSliderToPanel(JPanel panel, String title, JSlider slider) {
        JPanel p = new JPanel(new BorderLayout());
        p.setMaximumSize(new Dimension(1000, 70));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        p.add(lbl, BorderLayout.NORTH);
        p.add(slider, BorderLayout.CENTER);
        panel.add(p);
    }
}