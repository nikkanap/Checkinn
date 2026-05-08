package frontend;

import java.awt.*;
import javax.swing.*;

public class AdminPanel extends JPanel {
    private AdminLeftPanel leftPanel;
    private AdminRightPanel rightPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public AdminPanel(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(900, 600));

        System.out.println("Creating Right AdminPanel...");
        rightPanel = new AdminRightPanel();
        
        System.out.println("Creating Left AdminPanel...");
        leftPanel = new AdminLeftPanel(rightPanel, cardLayout, cardPanel, false);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.CENTER);
    }

    public void setAdminUser(String email) {
        System.out.println("AdminUserEmail: " + email);
        if(email.equals("superAdmin@gmail.com")){
            System.out.println("Enabling new Admin Button...");
            this.remove(leftPanel); 
            leftPanel = new AdminLeftPanel(rightPanel, cardLayout, cardPanel, true);
            this.add(leftPanel, BorderLayout.WEST); 
        } else {
            System.out.println("Disabling new Admin Button...");
            this.remove(leftPanel); 
            leftPanel = new AdminLeftPanel(rightPanel, cardLayout, cardPanel, false);
            this.add(leftPanel, BorderLayout.WEST); 
        }
        rightPanel.setAdminUser(email);
        leftPanel.perLoginAdmin();
        repaint();
        revalidate();
    }

}

