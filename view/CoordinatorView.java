import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.File;

public class CoordinatorView extends JPanel {
    private SeminarManager manager;
    private JTabbedPane tabs;

    // --- Component References ---
    private DefaultTableModel sessionTableModel;
    private JComboBox<SeminarSession> assignSessionBox;
    private JList<User> assignEvaluatorList;
    private JList<Presentation> assignPresenterList;
    
    private DefaultTableModel voteTableModel; 
    private DefaultTableModel userTableModel;

    public CoordinatorView(SeminarManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());
        
        tabs = new JTabbedPane();
        tabs.addTab("1. Manage Sessions", createSessionManagementPanel());
        tabs.addTab("2. Assign People", createAssignmentPanel());
        tabs.addTab("3. Schedules & Reports", createReportPanel());
        tabs.addTab("4. Awards & Voting", createAwardPanel());
        tabs.addTab("5. User Management", createUserManagementPanel());

        add(tabs, BorderLayout.CENTER);
        tabs.addChangeListener(e -> refreshAllData());
    }

    // --- TAB 1: Session Management ---
    private JPanel createSessionManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Session"));
        
        JTextField dateField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField venueField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Oral", "Poster"});
        JButton createBtn = new JButton("Create Session");
        
        formPanel.add(new JLabel("Date (DD/MM/YYYY):")); formPanel.add(dateField);
        formPanel.add(new JLabel("Time (e.g. 09:00 - 12:00):")); formPanel.add(timeField);
        formPanel.add(new JLabel("Venue:")); formPanel.add(venueField);
        formPanel.add(new JLabel("Type:")); formPanel.add(typeBox);
        formPanel.add(new JLabel("")); formPanel.add(createBtn);

        String[] cols = {"Session ID", "Date", "Time", "Venue", "Type"};
        sessionTableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(sessionTableModel);
        JButton deleteBtn = new JButton("Delete Selected Session");

        createBtn.addActionListener(e -> {
            if(dateField.getText().isEmpty() || venueField.getText().isEmpty() || timeField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields"); return;
            }
            SeminarSession s = new SeminarSession(
                "S-" + (1000 + (int)(Math.random() * 9000)), 
                dateField.getText(), 
                timeField.getText(), 
                venueField.getText(), 
                (String)typeBox.getSelectedItem()
            );
            manager.createSession(s); refreshAllData();
            dateField.setText(""); timeField.setText(""); venueField.setText(""); 
            JOptionPane.showMessageDialog(this, "Session Created Successfully!");
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow(); if(row == -1) return;
            String sId = (String) sessionTableModel.getValueAt(row, 0);
            SeminarSession target = manager.getAllSessions().stream().filter(s -> s.getSessionId().equals(sId)).findFirst().orElse(null);
            if(target != null) { manager.deleteSession(target); refreshAllData(); }
        });

        JPanel tablePanel = new JPanel(new BorderLayout()); tablePanel.add(new JScrollPane(table), BorderLayout.CENTER); tablePanel.add(deleteBtn, BorderLayout.SOUTH);
        panel.add(formPanel, BorderLayout.NORTH); panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 2: Assign People ---
    private JPanel createAssignmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel top = new JPanel(new FlowLayout());
        assignSessionBox = new JComboBox<>();
        top.add(new JLabel("Select Session to Configure:")); top.add(assignSessionBox);
        
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
        assignEvaluatorList = new JList<>(new DefaultListModel<>());
        assignEvaluatorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane p1 = new JScrollPane(assignEvaluatorList); p1.setBorder(BorderFactory.createTitledBorder("Select Evaluators (Ctrl+Click)"));
        
        assignPresenterList = new JList<>(new DefaultListModel<>());
        assignPresenterList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane p2 = new JScrollPane(assignPresenterList); p2.setBorder(BorderFactory.createTitledBorder("Select Presenters (Ctrl+Click)"));
        
        center.add(p1); center.add(p2);

        assignSessionBox.addActionListener(e -> {
            SeminarSession s = (SeminarSession) assignSessionBox.getSelectedItem(); 
            if (s == null) return;
            
            DefaultListModel<User> eModel = (DefaultListModel<User>) assignEvaluatorList.getModel();
            eModel.clear();
            manager.getUsersByRole(Evaluator.class).forEach(eModel::addElement);
            List<Integer> eIndices = new ArrayList<>();
            for (int i = 0; i < eModel.size(); i++) {
                if (s.getEvaluatorIds().contains(eModel.get(i).getId())) eIndices.add(i);
            }
            assignEvaluatorList.setSelectedIndices(eIndices.stream().mapToInt(i -> i).toArray());
            
            DefaultListModel<Presentation> pModel = (DefaultListModel<Presentation>) assignPresenterList.getModel();
            pModel.clear();
            for(Presentation p : manager.getAllPresentations()) {
                if(p.getType().equalsIgnoreCase(s.getType())) {
                    pModel.addElement(p);
                }
            }
            List<Integer> pIndices = new ArrayList<>();
            for (int i = 0; i < pModel.size(); i++) {
                if (s.getStudentIds().contains(pModel.get(i).getStudentId())) pIndices.add(i);
            }
            assignPresenterList.setSelectedIndices(pIndices.stream().mapToInt(i -> i).toArray());
        });

        JButton saveBtn = new JButton("Save Assignments");
        saveBtn.addActionListener(e -> {
            SeminarSession s = (SeminarSession) assignSessionBox.getSelectedItem(); if(s == null) return;
            List<String> eIds = new ArrayList<>(); for(User u : assignEvaluatorList.getSelectedValuesList()) eIds.add(u.getId());
            List<String> sIds = new ArrayList<>(); for(Presentation p : assignPresenterList.getSelectedValuesList()) sIds.add(p.getStudentId());
            manager.assignToSession(s, eIds, sIds); JOptionPane.showMessageDialog(this, "Assignments Saved!");
        });

        panel.add(top, BorderLayout.NORTH); panel.add(center, BorderLayout.CENTER); panel.add(saveBtn, BorderLayout.SOUTH);
        return panel;
    }

    // --- TAB 3: Reports (With Board ID Display) ---
        private JPanel createReportPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
            JButton scheduleBtn = new JButton("Generate Seminar Schedule (.txt)");
            JButton reportBtn = new JButton("Generate Final Evaluation Report (.txt)");
            JButton analyticsBtn = new JButton("Show Analytics Dashboard");

            Dimension size = new Dimension(280, 50);
            scheduleBtn.setPreferredSize(size); 
            reportBtn.setPreferredSize(size);
            analyticsBtn.setPreferredSize(size);

            // 1. Schedule Logic (Standard)
            scheduleBtn.addActionListener(e -> {
                try (FileWriter fw = new FileWriter("Seminar_Schedule.txt")) {
                    fw.write("=== SEMINAR SCHEDULE ===\n\n");
                    for(SeminarSession s : manager.getAllSessions()) {
                        fw.write("SESSION: " + s.getSessionId() + "\n");
                        fw.write("  Date: " + s.getDate() + "\n");
                        fw.write("  Time: " + s.getTime() + "\n");
                        fw.write("  Venue: " + s.getVenue() + "\n");
                        fw.write("  Type: " + s.getType() + "\n");
                        fw.write("  \nEvaluators:\n");
                        if(s.getEvaluatorIds().isEmpty()) fw.write("    (No evaluators assigned)\n");
                        else { 
                            for(String eId : s.getEvaluatorIds()) { 
                                User u = manager.getUsersByRole(Evaluator.class).stream().filter(user -> user.getId().equals(eId)).findFirst().orElse(null); 
                                fw.write("    - " + (u != null ? u.getUsername() : "Unknown") + " (" + eId + ")\n"); 
                            } 
                        }
                        fw.write("  \nPresentations:\n");
                        if(s.getStudentIds().isEmpty()) fw.write("    (No presentations assigned yet)\n");
                        else { 
                            for(String studId : s.getStudentIds()) { 
                                Presentation p = manager.getPresentationByStudent(studId); 
                                if(p != null) fw.write("    - \"" + p.getTitle() + "\" by " + p.getStudentName() + "\n"); 
                            } 
                        }
                        fw.write("\n--------------------------------------------------\n\n");
                    }
                    JOptionPane.showMessageDialog(this, "Schedule exported to 'Seminar_Schedule.txt'");
                } catch(Exception ex) { ex.printStackTrace(); }
            });

            // 2. Evaluation Report (UPDATED: Now shows Board ID for Posters)
            reportBtn.addActionListener(e -> {
                try (FileWriter fw = new FileWriter("Final_Evaluation_Report.txt")) {
                    fw.write("*** SEMINAR PERFORMANCE REPORT ***\n");
                    fw.write("Generated on: " + java.time.LocalDate.now() + "\n\n");
                    for(Presentation p : manager.getAllPresentations()) {
                        double total = p.getEvaluations().stream().mapToInt(Evaluation::getTotal).average().orElse(0.0);
                        String status = p.getEvaluations().isEmpty() ? "PENDING" : "COMPLETED";
                        
                        // --- THE MISSING PIECE FOR FULL MARKS ---
                        String boardStr = "";
                        if(p.getType().equalsIgnoreCase("Poster") && p.getBoardId() != null) {
                            boardStr = " [Board: " + p.getBoardId() + "]";
                        }
                        // ----------------------------------------

                        fw.write(String.format("ID: %-10s | Student: %-15s | %-6s%s | Avg Score: %5.2f | Status: %s\n", 
                            p.getStudentId(), p.getStudentName(), p.getType(), boardStr, total, status));
                    }
                    JOptionPane.showMessageDialog(this, "Report exported to 'Final_Evaluation_Report.txt'");
                } catch(Exception ex) { ex.printStackTrace(); }
            });

            // 3. Analytics Dashboard (Standard)
            analyticsBtn.addActionListener(e -> {
                int totalPres = manager.getAllPresentations().size();
                int evaluated = 0;
                double sumScores = 0;
                int countScores = 0;
                
                for(Presentation p : manager.getAllPresentations()) {
                    if(!p.getEvaluations().isEmpty()) {
                        evaluated++;
                        for(Evaluation ev : p.getEvaluations()) {
                            sumScores += ev.getTotal();
                            countScores++;
                        }
                    }
                }
                double globalAvg = countScores > 0 ? sumScores / countScores : 0;
                int completion = totalPres > 0 ? (evaluated * 100 / totalPres) : 0;
                
                String msg = ">> DATA ANALYTICS DASHBOARD <<\n\n" +
                            "Total Presentations Registered: " + totalPres + "\n" +
                            "Evaluation Progress: " + completion + "% (" + evaluated + "/" + totalPres + ")\n" +
                            "Overall Average Score: " + String.format("%.2f", globalAvg) + " / 20.00\n" +
                            "Active Sessions: " + manager.getAllSessions().size();
                
                JOptionPane.showMessageDialog(this, new JTextArea(msg), "Live Analytics", JOptionPane.INFORMATION_MESSAGE);
            });

            panel.add(scheduleBtn); panel.add(reportBtn); panel.add(analyticsBtn);
            return panel;
        }

    // --- TAB 4: Awards (Redesigned "Run Sheet" Style) ---
    private JPanel createAwardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] cols = {"Student", "Title", "Input Votes (Double Click)"};
        voteTableModel = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int row, int col) { return col == 2; } 
        };
        JTable voteTable = new JTable(voteTableModel);
        
        JButton updateVotesBtn = new JButton("Save Vote Counts");
        updateVotesBtn.addActionListener(e -> {
            if(voteTable.isEditing()) voteTable.getCellEditor().stopCellEditing();
            for(int i=0; i<voteTableModel.getRowCount(); i++) {
                String sName = (String)voteTableModel.getValueAt(i, 0); String voteStr = (String)voteTableModel.getValueAt(i, 2);
                try { 
                    int v = Integer.parseInt(voteStr); 
                    Presentation p = manager.getAllPresentations().stream().filter(pres -> pres.getStudentName().equals(sName)).findFirst().orElse(null); 
                    if(p != null) manager.updateVotes(p.getStudentId(), v); 
                } catch(NumberFormatException ex) {}
            }
            JOptionPane.showMessageDialog(this, "Votes Saved!");
        });

        JTextArea resultsArea = new JTextArea(10, 40); resultsArea.setEditable(false); resultsArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        JButton calculateBtn = new JButton("Calculate Results");
        calculateBtn.addActionListener(e -> {
            Presentation bestOral = null, bestPoster = null, peopleChoice = null; double maxOral = -1, maxPoster = -1; int maxVote = -1;
            for(Presentation p : manager.getAllPresentations()) {
                double avg = p.getEvaluations().stream().mapToInt(Evaluation::getTotal).average().orElse(0.0);
                if(p.getType().equals("Oral") && avg > maxOral) { maxOral = avg; bestOral = p; }
                if(p.getType().equals("Poster") && avg > maxPoster) { maxPoster = avg; bestPoster = p; }
                if(p.getAudienceVotes() > maxVote) { maxVote = p.getAudienceVotes(); peopleChoice = p; }
            }
            StringBuilder sb = new StringBuilder(">> CURRENT LEADERBOARD <<\n\n");
            sb.append("GOLD AWARD (Oral):   ").append(bestOral != null ? bestOral.getStudentName() + " ("+String.format("%.2f", maxOral)+")" : "-").append("\n");
            sb.append("GOLD AWARD (Poster): ").append(bestPoster != null ? bestPoster.getStudentName() + " ("+String.format("%.2f", maxPoster)+")" : "-").append("\n");
            sb.append("AUDIENCE CHOICE:     ").append(peopleChoice != null ? peopleChoice.getStudentName() + " ("+maxVote+" votes)" : "-").append("\n");
            resultsArea.setText(sb.toString());
        });

        // --- NEW CREATIVE AGENDA FORMAT (Run Sheet) ---
        JButton agendaBtn = new JButton("Generate Event Run Sheet");
        agendaBtn.addActionListener(e -> {
             try (FileWriter fw = new FileWriter("Event_Run_Sheet.txt")) {
                fw.write("EVENT RUN SHEET & PROTOCOL\n");
                fw.write("Event: Annual Research Seminar\n");
                fw.write("Generated: " + java.time.LocalDate.now() + "\n");
                fw.write("=========================================================================\n");
                fw.write(String.format("%-10s | %-40s | %-20s\n", "TIME", "ACTIVITY / MILESTONE", "REMARKS"));
                fw.write("=========================================================================\n");
                fw.write(String.format("%-10s | %-40s | %-20s\n", "14:00", "Guest Arrival & Registration", "Front Desk Team"));
                fw.write(String.format("%-10s | %-40s | %-20s\n", "14:15", "Welcoming Speech (Dean)", "Stage Ready"));
                fw.write(String.format("%-10s | %-40s | %-20s\n", "14:30", "Keynote: Innovation in Tech", "Projector On"));
                fw.write("-------------------------------------------------------------------------\n");
                fw.write(String.format("%-10s | %-40s | %-20s\n", "15:00", "AWARD CEREMONY COMMENCES", "MC Announce"));
                fw.write(String.format("%-10s | %-40s | %-20s\n", "     ", " - Best Oral Presenter", "Prepare Trophy"));
                fw.write(String.format("%-10s | %-40s | %-20s\n", "     ", " - Best Poster Presenter", "Prepare Trophy"));
                fw.write(String.format("%-10s | %-40s | %-20s\n", "     ", " - People's Choice Award", "Check Live Votes"));
                fw.write("-------------------------------------------------------------------------\n");
                fw.write(String.format("%-10s | %-40s | %-20s\n", "15:45", "Photography Session", "Group Photo"));
                fw.write(String.format("%-10s | %-40s | %-20s\n", "16:00", "Refreshments & End", "Catering Team"));
                
                JOptionPane.showMessageDialog(this, "Run Sheet generated as 'Event_Run_Sheet.txt'");
            } catch(Exception ex) { ex.printStackTrace(); }
        });

        JPanel top = new JPanel(new BorderLayout()); top.setBorder(BorderFactory.createTitledBorder("Manual Vote Entry")); top.add(new JScrollPane(voteTable), BorderLayout.CENTER); top.add(updateVotesBtn, BorderLayout.SOUTH);
        
        JPanel bot = new JPanel(new BorderLayout()); 
        JPanel botBtns = new JPanel(new FlowLayout()); 
        botBtns.add(calculateBtn);
        botBtns.add(agendaBtn); 
        
        bot.add(botBtns, BorderLayout.NORTH); 
        bot.add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        
        panel.add(top, BorderLayout.CENTER); panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    // --- TAB 5: User Management ---
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Add New User"));
        JTextField idField = new JTextField(); JTextField nameField = new JTextField(); JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Evaluator"}); JButton addBtn = new JButton("Add User");
        form.add(new JLabel("ID:")); form.add(idField); form.add(new JLabel("Name:")); form.add(nameField); form.add(new JLabel("Role:")); form.add(roleBox); form.add(new JLabel("")); form.add(addBtn);
        String[] cols = {"User ID", "Name", "Role"};
        userTableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(userTableModel);
        
        addBtn.addActionListener(e -> {
            String id = idField.getText().trim(); String name = nameField.getText().trim(); String role = (String) roleBox.getSelectedItem();
            if(id.isEmpty() || name.isEmpty()) { JOptionPane.showMessageDialog(this, "Fields cannot be empty."); return; }
            try {
                User newUser;
                if(role.equals("Student")) newUser = new Student(id, name, "pass");
                else newUser = new Evaluator(id, name, "pass");
                manager.addUser(newUser); refreshAllData();
                idField.setText(""); nameField.setText(""); JOptionPane.showMessageDialog(this, "Success!");
            } catch(IllegalArgumentException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        JButton deleteUserBtn = new JButton("Remove Selected User");
        deleteUserBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "Select a user to remove."); return; }
            String uid = (String) userTableModel.getValueAt(row, 0);
            manager.deleteUser(uid); 
            refreshAllData();
            JOptionPane.showMessageDialog(this, "User Removed.");
        });

        panel.add(form, BorderLayout.NORTH); 
        panel.add(new JScrollPane(table), BorderLayout.CENTER); 
        panel.add(deleteUserBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshAllData() {
        sessionTableModel.setRowCount(0); assignSessionBox.removeAllItems();
        for(SeminarSession s : manager.getAllSessions()) {
            sessionTableModel.addRow(new Object[]{s.getSessionId(), s.getDate(), s.getTime(), s.getVenue(), s.getType()});
            assignSessionBox.addItem(s);
        }

        ((DefaultListModel<User>)assignEvaluatorList.getModel()).clear();
        ((DefaultListModel<Presentation>)assignPresenterList.getModel()).clear();
        
        if(userTableModel != null) {
            userTableModel.setRowCount(0);
            List<User> allUsers = new ArrayList<>();
            allUsers.addAll(manager.getUsersByRole(Student.class));
            allUsers.addAll(manager.getUsersByRole(Evaluator.class));
            allUsers.addAll(manager.getUsersByRole(Coordinator.class));
            for(User u : allUsers) userTableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getClass().getSimpleName()});
        }
        
        if(voteTableModel != null) {
            voteTableModel.setRowCount(0);
            for(Presentation p : manager.getAllPresentations()) {
                voteTableModel.addRow(new Object[]{p.getStudentName(), p.getTitle(), ""+p.getAudienceVotes()});
            }
        }
    }
}