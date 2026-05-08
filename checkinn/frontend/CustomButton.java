package frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.border.LineBorder;

public class CustomButton extends JButton {

    public CustomButton(String text, int text_size, int width, int height, Color backgroundColor, Color textColor) {
        super(text);

        Font custom_font = CustomFont.LoadCustomFont(text_size);
        this.setFont(custom_font);
        this.setBorder(new LineBorder(backgroundColor, 2, true));
        this.setFocusPainted(false);
        this.setBackground(backgroundColor);
        this.setForeground(textColor); // Set text color here
        this.setPreferredSize(new Dimension(width, height));
    }
}
