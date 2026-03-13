package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class MessageRender extends JPanel implements ListCellRenderer<Message> {

    private JPanel wrapper; // El contenedor maestro (como el de tu amigo)
    private JPanel bubblePanel;
    private JLabel textLabel;
    private JLabel timeLabel;
    private JLabel imageLabel; // Ahora sí lo vamos a inicializar

    // Caché para no recalcular la imagen cientos de veces
    private Map<String, ImageIcon> imageCache = new HashMap<>();

    public MessageRender() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // 1. Inicializar etiquetas
        textLabel = new JLabel();
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        imageLabel = new JLabel(); // ¡CORREGIDO!
        imageLabel.setOpaque(false);

        // 2. Crear la burbuja redondeada
        bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // 3. Crear el Wrapper principal (Truco de tu amigo) UNA SOLA VEZ
        wrapper = new JPanel(new FlowLayout());
        wrapper.setOpaque(false);
        wrapper.add(bubblePanel);

        add(wrapper, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message msg, int index, boolean isSelected, boolean cellHasFocus) {

        // Limpiamos la burbuja en cada iteración
        bubblePanel.removeAll();

        boolean isMe = !msg.getStatusMessage().toString().equals("RECEIVED");

        // --- MANEJO DE ALINEACIÓN Y COLORES (Al estilo de tu amigo) ---
        if (isMe) {
            bubblePanel.setBackground(new Color(220, 248, 198));
            textLabel.setForeground(Color.BLACK);
            timeLabel.setText("Yo - " + msg.getStatusMessage().toString());
            timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            // Reutilizamos el layout en lugar de crear paneles nuevos
            ((FlowLayout) wrapper.getLayout()).setAlignment(FlowLayout.RIGHT);
        } else {
            bubblePanel.setBackground(Color.WHITE);
            textLabel.setForeground(Color.BLACK);
            timeLabel.setText(msg.getDate());
            timeLabel.setHorizontalAlignment(SwingConstants.LEFT);

            // Reutilizamos el layout en lugar de crear paneles nuevos
            ((FlowLayout) wrapper.getLayout()).setAlignment(FlowLayout.LEFT);
        }

        // --- MANEJO DE CONTENIDO (Texto vs Imagen con Caché) ---
        if (msg.getTypeMessage() == TypeMessage.IMAGE) {
            String msgId = msg.getIdMessage();

            // Si la imagen ya fue procesada, la sacamos de la memoria rápida (¡Lag CERO!)
            if (imageCache.containsKey(msgId)) {
                imageLabel.setIcon(imageCache.get(msgId));
                bubblePanel.add(imageLabel, BorderLayout.CENTER);
            } else {
                // Si es nueva, hacemos el trabajo pesado y la guardamos
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(msg.getBody());
                    ImageIcon icon = new ImageIcon(imageBytes);
                    Image img = icon.getImage();

                    int maxWidth = 250;
                    int maxHeight = 250;
                    int width = img.getWidth(null);
                    int height = img.getHeight(null);

                    if (width > maxWidth || height > maxHeight) {
                        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
                        width = Math.round(width * ratio);
                        height = Math.round(height * ratio);
                        img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(img);
                    }

                    // Guardamos en caché para la próxima vez
                    imageCache.put(msgId, icon);

                    imageLabel.setIcon(icon);
                    bubblePanel.add(imageLabel, BorderLayout.CENTER);
                } catch (Exception e) {
                    textLabel.setText("<html><p style='width: 250px;'><i>[Imagen no disponible]</i></p></html>");
                    bubblePanel.add(textLabel, BorderLayout.CENTER);
                }
            }
        } else {
            // Es un mensaje de texto normal
            textLabel.setText("<html><p style='width: 250px;'>" + msg.getBody() + "</p></html>");
            bubblePanel.add(textLabel, BorderLayout.CENTER);
        }

        bubblePanel.add(timeLabel, BorderLayout.SOUTH);

        // --- SELECCIÓN ---
        if (isSelected) {
            setBackground(new Color(230, 230, 230));
            setOpaque(true);
        } else {
            setOpaque(false);
        }

        return this;
    }
}