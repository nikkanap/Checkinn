package frontend;

import java.awt.*;
import javax.swing.*;

public class RoomStatusPanel extends JPanel {
    private JLabel totalLabel;
    private JLabel availableLabel;
  
    public RoomStatusPanel (String roomType) {
        repaint();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel title = new JLabel(" "+ roomType);
        title.setFont(CustomFont.LoadCustomFont(15));
        title.setForeground(new Color(0x4a75e8));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(new Color(0xcccccc));
        
        totalLabel = new JLabel("  Total: 0");
        availableLabel = new JLabel("  Available: 0");

        for(JLabel label: new JLabel[]{totalLabel, availableLabel}) {
            label.setFont(CustomFont.LoadCustomFont(12));
            label.setAlignmentX(LEFT_ALIGNMENT);
        }

        this.add(title);
        this.add(Box.createVerticalStrut(4));
        this.add(separator);
        this.add(Box.createVerticalStrut(4));
        this.add(totalLabel);
        this.add(availableLabel);
    }

    public JLabel getTotal() {
        return totalLabel;
    }

    public JLabel getAvailable() {
        return availableLabel;
    }

    public void update(int available, int total) {
        totalLabel.setText("  Total: " + total);
        availableLabel.setText("  Available: " + available);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // proceed if bg_image is not null
        Graphics2D g2d = (Graphics2D) g;
    }
}
