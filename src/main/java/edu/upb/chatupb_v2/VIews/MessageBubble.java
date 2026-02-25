package edu.upb.chatupb_v2.VIews;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MessageBubble extends JPanel {

    public MessageBubble(String text, boolean isOwnMessage) {

        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel label = new JLabel("<html><p style='width: 200px'>" + text + "</p></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel bubble = new JPanel();
        bubble.setLayout(new BorderLayout());
        bubble.add(label, BorderLayout.CENTER);
        bubble.setBorder(new EmptyBorder(5, 10, 5, 10));

        if (isOwnMessage) {
            bubble.setBackground(new Color(220, 248, 198)); // verde WhatsApp
        } else {
            bubble.setBackground(new Color(240, 240, 240)); // gris
        }

        bubble.setOpaque(true);

        setLayout(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT));
        add(bubble);
    }
}