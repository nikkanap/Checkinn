package frontend;
import backend.*;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LogIn extends JPanel implements ActionListener{

    private Image bg_image;
    private JButton loginButton,  backButton;
    private static JTextField emailField;
    private static JPasswordField passwordField;
    private CardLayout cardLayout;
    private JPanel currentPanel, cardPanel;
    private AdminPanel adminPanel;

    public LogIn(CardLayout cardLayout, JPanel cardPanel, AdminPanel adminPanel) {
        this.currentPanel = this;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel; 
        this.adminPanel = adminPanel;

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        bg_image = new ImageIcon("./files/login.png").getImage();

        Font customFont1 = CustomFont.LoadCustomFont(25);
        JLabel loginLabel = new JLabel("Log in");
        loginLabel.setFont(customFont1);
        loginLabel.setForeground(new Color(0x4a75e8));
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 660);
        add(loginLabel, gbc);

        JLabel adminLabel = new JLabel("as administrator");
        adminLabel.setFont(customFont1);
        adminLabel.setForeground(new Color(0x191919));
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 370);
        add(adminLabel, gbc);

        Font customFont2 = CustomFont.LoadCustomFont(15);
        JLabel emailLabel = new JLabel("Enter email: ");
        emailLabel.setFont(customFont2);
        emailLabel.setForeground(new Color(0x535353));
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 635);
        add(emailLabel, gbc);

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 25));
        emailField.setFont(customFont2);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 200);
        add(emailField, gbc);
        emailField.addActionListener(this);

        JLabel passwordLabel = new JLabel("Enter password: ");
        passwordLabel.setFont(customFont2);
        passwordLabel.setForeground(new Color(0x535353));
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 0, 605);
        add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 25));
        passwordField.setFont(customFont2);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 0, 200);
        add(passwordField, gbc);
        passwordField.addActionListener(this);

        loginButton = new CustomButton("Log in", 15, 365, 30, new Color(0x4a75e8), new Color(0xf2f2f2));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 0, 0, 370);
        add(loginButton, gbc);
        loginButton.addActionListener(this);

        backButton = new CustomButton("Back", 15, 365, 30, new Color(0xc5c5c5), new Color(0x535353));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 0, 370);
        add(backButton, gbc);
        backButton.addActionListener(this);
        
        setVisible(true);
    }

    // Used to clear the fields
    public static void clearFields(){
        emailField.setText("");
        passwordField.setText("");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // skip the next set of code if bg_image is null
        if (bg_image == null) { return; }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(bg_image, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == backButton) { 
            cardLayout.show(cardPanel, "Main Page"); 
            return;
        }

        String email = emailField.getText().trim(); 
        String password = new String(passwordField.getPassword()); 

        boolean areAllFieldsFilled = email.isBlank() || password.isBlank(); 
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
                "Invalid email format.", 
                "Invalid Email Format", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        boolean valid = CrudAndOthers.staffLogIn(email, password); 
        if(!valid){
            JOptionPane.showMessageDialog(
                null,                              
                "Invalid login, try again.",      
                "Login Failed",                  
                JOptionPane.ERROR_MESSAGE       
            );
            return;
        }

        // Proceed here if all inputs are valid
        adminPanel.setAdminUser(email);
        adminPanel.revalidate();
        adminPanel.repaint();
        cardLayout.show(cardPanel,"Admin Page");
        clearFields();
    }
}
