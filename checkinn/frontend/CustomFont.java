package frontend;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

public class CustomFont {

    public static Font LoadCustomFont(int text_size) {
        Font custom_font;
        try {
            custom_font = Font.createFont(Font.TRUETYPE_FONT, new File("./files/bricgro-regular.ttf"));
            custom_font = custom_font.deriveFont(Font.BOLD, text_size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            System.out.println("Error loading custom font. Default font will be used.");
            custom_font = new Font("Arial", Font.BOLD, 16);
        }
        return custom_font;
    }

}