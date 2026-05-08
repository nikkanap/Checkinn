package frontend;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

// MainPagePanel: Welcome screen where we add ADMIN and BOOK NOW button
public class MainPagePanel extends JPanel {

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private Image bg_image;
    private JPanel upperPanel, lowerPanel;

    public MainPagePanel(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.setLayout(new BorderLayout());

        // Set JPanel background image
        bg_image = new ImageIcon("./files/main_bg.png").getImage();
        setUpperPanel();
        setLowerPanel();

        // Add upperPanel and lowerPanel to frame
        this.add(upperPanel, BorderLayout.NORTH);
        this.add(lowerPanel, BorderLayout.SOUTH);
    }

    private void setUpperPanel() {

        // Set upperPanel layout
        GridBagConstraints gbc = new GridBagConstraints();
        upperPanel = new JPanel();
        upperPanel.setOpaque(false);
        upperPanel.setPreferredSize(new Dimension(900, 250));
        upperPanel.setLayout(new GridBagLayout());

        // Create ADMIN button
        CustomButton adminButton = new CustomButton("ADMIN", 13, 100, 30, new Color(0xd9d9d9), new Color(0x535353));

        // Add action listener to ADMIN button
        adminButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Log In");
            LogIn.clearFields();
        });

        // Add ADMIN button to upperPanel
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 720, 110, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        upperPanel.add(adminButton, gbc);

    }

    private void setLowerPanel() {

        // Set lowerPanel layout
        GridBagConstraints gbc = new GridBagConstraints();
        lowerPanel = new JPanel();
        lowerPanel.setOpaque(false);
        lowerPanel.setPreferredSize(new Dimension(0, 150));
        lowerPanel.setLayout(new GridBagLayout());

        // Create BOOK NOW button
        CustomButton bookButton = new CustomButton("BOOK NOW", 13, 100, 30, new Color(0x4a75e8), new Color(0xf9f9f9));

        // Add action listener to BOOK NOW button
        bookButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Booking");
        });

        // Add BOOK NOW button to lowerPanel
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        lowerPanel.add(bookButton, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Don't proceed if bg_image is null
        if (bg_image == null)
            return;

        // Proceed if bg_image is not null
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(bg_image, 0, 0, getWidth(), getHeight(), this);
    }
}
