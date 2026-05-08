package frontend;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import backend.CrudAndOthers;

public class CreateNewAdmin extends JPanel implements ActionListener{

    private static JTextField fNameField, lNameField, emaField;
    private static JPasswordField pwField;
    private JButton submitButton, backButton;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Image bg_image;

    public CreateNewAdmin(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel; 

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        bg_image = new ImageIcon("./files/signup2.png").getImage();

        Font customFont1 = CustomFont.LoadCustomFont(23);
        JLabel signUpLabel = new JLabel("Create a New Admin");
        signUpLabel.setForeground(new Color(0x4a75e8));
        signUpLabel.setFont(customFont1);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 50, 10, 660);
        add(signUpLabel, gbc);

        Font customFont2 = CustomFont.LoadCustomFont(15);
        JLabel fNameLabel = new JLabel("First Name: ");
        fNameLabel.setForeground(new Color(0x535353));
        fNameLabel.setFont(customFont2);
        gbc.insets = new Insets(10, 50, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 1;
        add(fNameLabel, gbc);

        fNameField = new JTextField();
        fNameField.setPreferredSize(new Dimension(200, 25));
        fNameField.setFont(customFont2);
        fNameField.addActionListener(this);
        gbc.insets = new Insets(10, 150, 0, 0);
        add(fNameField, gbc);

        JLabel lNameLabel = new JLabel("Last Name: ");
        lNameLabel.setForeground(new Color(0x535353));
        lNameLabel.setFont(customFont2);
        gbc.insets = new Insets(10, 50, 0, 605);
        gbc.gridy = 2;
        add(lNameLabel, gbc);

        lNameField = new JTextField();
        lNameField.setPreferredSize(new Dimension(200, 25));
        lNameField.setFont(customFont2);
        lNameField.addActionListener(this);
        gbc.insets = new Insets(10, 150, 0, 200);
        add(lNameField, gbc);

        JLabel email = new JLabel("Email: ");
        email.setForeground(new Color(0x535353));
        email.setFont(customFont2);
        gbc.insets = new Insets(10, 50, 0, 605);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 3;
        add(email, gbc);

        emaField = new JTextField();
        emaField.setPreferredSize(new Dimension(200, 25));
        emaField.setFont(customFont2);
        emaField.addActionListener(this);
        gbc.insets = new Insets(10, 150, 0, 200);
        add(emaField, gbc);

        JLabel pword = new JLabel("Password: ");
        pword.setFont(customFont2);
        pword.setForeground(new Color(0x535353));
        gbc.insets = new Insets(10, 50, 0, 605);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 4;
        add(pword, gbc);

        pwField = new JPasswordField();
        pwField.setPreferredSize(new Dimension(200, 25));
        emaField.setFont(customFont2);
        pwField.addActionListener(this);
        gbc.insets = new Insets(10, 150, 0, 200);
        gbc.gridy = 4;
        add(pwField, gbc);

        submitButton = new CustomButton("Submit", 15, 300, 30, new Color(0x4a75e8), new Color(0xf2f2f2));
        submitButton.addActionListener(this);
        gbc.insets = new Insets(15, 50, 0, 370);        
        gbc.gridy = 5;
        add(submitButton, gbc);

        backButton = new CustomButton("Back", 15, 300, 30, new Color(0x4a75e8), new Color(0xf2f2f2));
        backButton.addActionListener(this);
        gbc.insets = new Insets(15, 50, 0, 370);
        gbc.gridy = 6;
        add(backButton, gbc);

        setVisible(true);
    }

    // Clear the fields
    public static void clearFields(){
        JTextField[] fields = new JTextField[] {fNameField, lNameField, emaField, pwField};
        for(JTextField field : fields)
            field.setText("");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bg_image != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(bg_image, 0, 0, getWidth(), getHeight(), this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String panelName = "";
        if(e.getSource() == fNameField) { 
            System.out.println("email: " + emaField.getText()); 
            return;
        } else if(e.getSource() == lNameField) { 
            System.out.println("Password: " + lNameField.getText()); 
            return;
        } else if(e.getSource() == submitButton) {
            String firstName = fNameField.getText();
            String lastName = lNameField.getText();
            String email = emaField.getText().trim();
            String password = new String(pwField.getPassword());

            // Error checking: cannot proceed when fields are incomplete
            boolean areAllFieldsFilled = firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank();
            if(areAllFieldsFilled){
                JOptionPane.showMessageDialog(
                    null, 
                    "Please fill up all the fields.", 
                    "Incomplete Information", 
                    JOptionPane.ERROR_MESSAGE
                    );
                return;
            }
            
            if (!email.endsWith("@gmail.com") || email.length() < "@gmail.com".length() + 3 || email.contains(" ")) {
                JOptionPane.showMessageDialog(
                    null, 
                    "Email must be a valid Gmail address with at least 3 characters before '@gmail.com' and do not contain any spaces.", 
                    "Invalid Email Format", 
                    JOptionPane.ERROR_MESSAGE
                    );
                return;
            }

            // Confirmation: either the user proceeds or not
            int confirmed = JOptionPane.showConfirmDialog(
                null, 
                "Is all the information entered accurate?",
                "Submit Information Confirmation", 
                JOptionPane.OK_CANCEL_OPTION
            );
            if(confirmed == JOptionPane.CANCEL_OPTION){ return; }

            // Create a new staff
            try {
                CrudAndOthers.createStaffAccount(lastName, firstName, password, email);
                System.out.println("Successfully created staff.");
            } catch (SQLException e1) {
                e1.printStackTrace();
                System.out.println("Failed to create staff.");
            }
            panelName = "Main Page";   
        }else if(e.getSource() == backButton){ panelName = "Admin Page"; }

        cardLayout.show(cardPanel, panelName); 
        clearFields();
    }
}

