package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.model.entities.Contact;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ContactRender extends JLabel implements ListCellRenderer<Contact> {

    protected static final Font SELECTED_FONT = new Font("Segoe UI", Font.BOLD, 14);
    protected static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public ContactRender() {
        setOpaque(true); // ¡MUY IMPORTANTE! Si es false, el background no se pinta
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Contact> list, Contact contact, int index, boolean isSelected, boolean cellHasFocus) {

        ImageIcon imageIcon;
        try {
            if (contact.isStateConnect()) {
                imageIcon = new ImageIcon(getClass().getResource("/images/on-line.png"));
            } else {
                imageIcon = new ImageIcon(getClass().getResource("/images/off-line.png"));
            }
            Image imgScaled = imageIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(imgScaled));
        } catch (Exception e) {
            System.out.println("No se encontró la imagen: " + e.getMessage());
            setIcon(null);
        }

        setText("<html><p style='margin-left: 5px;'>" + contact.getName() + "</p></html>");

        if (isSelected) {
            setBackground(new Color(180, 215, 255));
            setForeground(Color.BLACK);
            setFont(SELECTED_FONT);
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setFont(NORMAL_FONT);
        }

        return this;
    }
}