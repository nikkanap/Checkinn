package frontend;

import backend.ConfigReader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class DbSetupDialog extends JDialog {
    private JTextField urlField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean setupComplete = false;
    private ConfigReader config;
    
    public DbSetupDialog(Frame parent, ConfigReader config) {
        super(parent, "Database Configuration", true);
        this.config = config;
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel instructionLabel = new JLabel(
            "<html><body><p>Please enter your MySQL database credentials.</p>" +
            "<p>The application needs these to connect to your database.</p></body></html>"
            );
        instructionLabel.setPreferredSize(new Dimension(400, 60));
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 10));
        
        urlField = new JTextField(config.getDbUrl());
        usernameField = new JTextField(config.getDbUsername());
        passwordField = new JPasswordField();
        
        formPanel.add(new JLabel("Database URL:"));
        formPanel.add(urlField);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton testButton = new JButton("Test Connection");
        JButton saveButton = new JButton("Save & Continue");
        JButton cancelButton = new JButton("Cancel");
        
        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(instructionLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        //actions
        testButton.addActionListener(e -> testConnection());
        saveButton.addActionListener(e -> saveConfig());
        cancelButton.addActionListener(e -> {
            dispose();
            System.exit(0);
        });
        
        setContentPane(mainPanel);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!setupComplete) {
                    int response = JOptionPane.showConfirmDialog(
                        DbSetupDialog.this,
                        "The application needs database credentials to run.\nAre you sure you want to exit?",
                        "Exit Application",
                        JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) { System.exit(0); }
                } else { dispose(); }
            }
        });
        
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void testConnection() {
        String url = urlField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                JOptionPane.showMessageDialog(this,
                    "Connection successful!",
                    "Test Connection",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                "MySQL JDBC driver not found. Please make sure it's in your classpath.",
                "Driver Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Connection failed: " + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void saveConfig() {
        String url = urlField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (url.isBlank() || username.isBlank()) {
            JOptionPane.showMessageDialog(this,
                "URL and username cannot be empty.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                // Connection successful, save config
                config.updateConfig(username, password);
                setupComplete = true;
                dispose();
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                "MySQL JDBC driver not found. Please make sure it's in your classpath.",
                "Driver Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            int response = JOptionPane.showConfirmDialog(this,
                "Connection failed: " + e.getMessage() + "\nDo you still want to save these credentials?",
                "Connection Error",
                JOptionPane.YES_NO_OPTION);
                
            if (response == JOptionPane.YES_OPTION) {
                config.updateConfig(username, password);
                setupComplete = true;
                dispose();
            }
        }
    }
    
    public boolean isSetupComplete() {
        return setupComplete;
    }
}
