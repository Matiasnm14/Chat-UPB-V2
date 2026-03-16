package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Contact;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

public class ContactRender extends JLabel implements ListCellRenderer<Contact> {

    protected static final Font SELECTED_FONT = new Font("Segoe UI", Font.BOLD, 14);
    protected static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    // 1. Guardamos los íconos en memoria para que no haya Lag
    private ImageIcon iconOnline;
    private ImageIcon iconOffline;

    public ContactRender() {
        setOpaque(true);
        setBorder(new EmptyBorder(5, 5, 5, 5)); // Un poco de espacio para que no se vea apretado

        // 2. Cargamos las imágenes UNA SOLA VEZ al iniciar
        try {
            URL urlOnline = getClass().getResource("/images/on-line.png");
            URL urlOffline = getClass().getResource("/images/off-line.png");

            // Validamos que la ruta exista antes de intentar dibujarla
            if (urlOnline != null) {
                Image imgOn = new ImageIcon(urlOnline).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
                iconOnline = new ImageIcon(imgOn);
            } else {
                System.err.println("❌ ALERTA: No se encontró la imagen /images/on-line.png");
            }

            if (urlOffline != null) {
                Image imgOff = new ImageIcon(urlOffline).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
                iconOffline = new ImageIcon(imgOff);
            } else {
                System.err.println("❌ ALERTA: No se encontró la imagen /images/off-line.png");
            }
        } catch (Exception e) {
            System.err.println("Error procesando las imágenes: " + e.getMessage());
        }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Contact> list, Contact contact, int index, boolean isSelected, boolean cellHasFocus) {

        // 3. Asignar el ícono desde la memoria rapidísimo
        if (contact.isStateConnect()) {
            setIcon(iconOnline);
        } else {
            setIcon(iconOffline);
        }

        // El texto (Quitamos el HTML porque JLabel ya soporta texto plano y alinea el ícono mejor)
        setText("  " + contact.getName());

        // 4. Colores de selección
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