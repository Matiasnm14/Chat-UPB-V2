package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage; // Asegúrate de importar tu enum

import javax.swing.*;
import java.awt.*;
import java.util.Base64;
import javax.swing.border.EmptyBorder;

public class ChatRender extends JPanel implements ListCellRenderer<Message> {

    private JLabel messageLabel;
    private JLabel imageLabel;
    private JPanel container;

    private JPanel bubblePanel;
    private JLabel metaLabel;

    public ChatRender() {
        setLayout(new BorderLayout());
        setOpaque(false); // Transparente para no tapar el fondo de la lista

        // Configuramos la etiqueta para texto
        messageLabel = new JLabel();
        messageLabel.setOpaque(false);
        messageLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Configuramos la etiqueta para imágenes
        imageLabel = new JLabel();
        imageLabel.setOpaque(false);
        imageLabel.setBorder(new EmptyBorder(8, 12, 8, 12));

        metaLabel = new JLabel();
        metaLabel.setFont(new Font("Arial", Font.ITALIC, 10)); // Letra más pequeña
        metaLabel.setForeground(Color.GRAY); // Color sutil

        bubblePanel = new JPanel();
        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setOpaque(true); // Ahora el color de fondo irá aquí
        bubblePanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        container = new JPanel(new FlowLayout());
        container.setOpaque(false);
        container.add(bubblePanel);

        add(container, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message msg, int index, boolean isSelected, boolean cellHasFocus) {


        bubblePanel.removeAll();


        if (msg.getTypeMessage() == TypeMessage.IMAGE) {
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

                imageLabel.setIcon(icon);
                bubblePanel.add(imageLabel, BorderLayout.CENTER);
            } catch (Exception e) {

                messageLabel.setText("<html><p style='width: 250px;'><i>[Imagen no disponible]</i></p></html>");
                bubblePanel.add(messageLabel, BorderLayout.CENTER);
            }
        } else {

            messageLabel.setText("<html><p style='width: 250px;'>" + msg.getBody() + "</p></html>");
            bubblePanel.add(messageLabel, BorderLayout.CENTER);
        }


        boolean isMine = msg.getStatusMessage() != StatusMessage.READ;

        if (isMine) {

            ((FlowLayout) container.getLayout()).setAlignment(FlowLayout.RIGHT);
            bubblePanel.setBackground(new Color(220, 248, 198));
            String state = msg.getStatusMessage() == StatusMessage.RECEIVED ? "Leido" : "Enviado";
            metaLabel.setText(msg.getDate() + " " + state);
        } else {

            ((FlowLayout) container.getLayout()).setAlignment(FlowLayout.LEFT);
            bubblePanel.setBackground(new Color(240, 240, 240));
            metaLabel.setText(msg.getDate());
        }

        metaLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);


        bubblePanel.add(metaLabel, BorderLayout.SOUTH);

        return this;
    }
}