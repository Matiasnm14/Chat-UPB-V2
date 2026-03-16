package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Base64;

public class ChatRender implements ListCellRenderer<Message> { // ¡Cambio importante aquí!
    private JPanel panel;
    private JPanel bubblePanel;
    private JLabel messageLabel;
    private JLabel metaLabel;

    public ChatRender() {
        // Contenedor principal de la fila (debe ser transparente)
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.setOpaque(false); // VITAL: Mantiene la fila transparente

        // Contenedor que simula la burbuja de chat (debe ser sólido)
        bubblePanel = new JPanel();
        bubblePanel.setLayout(new BorderLayout(5, 5));
        bubblePanel.setBorder(new EmptyBorder(8, 12, 5, 12));
        bubblePanel.setOpaque(true); // VITAL: Para que se lea el texto

        // Etiqueta para el contenido principal
        messageLabel = new JLabel();

        // Etiqueta para la metadata (hora, estado)
        metaLabel = new JLabel();
        metaLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        metaLabel.setForeground(Color.GRAY);
        metaLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message msg, int index, boolean isSelected, boolean cellHasFocus) {

        // Limpieza de paneles
        panel.removeAll();
        bubblePanel.removeAll();
        messageLabel.setIcon(null);
        messageLabel.setText("");

        // Lógica para saber si es mío
        boolean isMine = msg.getStatusMessage() != StatusMessage.READ;

        // Color base de la burbuja
        Color bubbleColor = isMine ? new Color(220, 248, 198) : Color.WHITE;

        // Si seleccionamos el mensaje, oscurecemos un poco la burbuja (pero NO el fondo de la fila)
        if (isSelected) {
            bubblePanel.setBackground(new Color(200, 220, 240));
        } else {
            bubblePanel.setBackground(bubbleColor);
        }

        // Lógica de tipos de mensaje
        if (msg.getTypeMessage() == TypeMessage.UNIQUE) {
            messageLabel.setText("🔒 Ver mensaje oculto");
            bubblePanel.setBackground(new Color(255, 230, 230));
            messageLabel.setForeground(new Color(180, 0, 0));
            messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            bubblePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        } else if (msg.getTypeMessage() == TypeMessage.IMAGE) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(msg.getBody());
                ImageIcon imageIcon = new ImageIcon(imageBytes);
                Image image = imageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                messageLabel.setIcon(new ImageIcon(image));
            } catch (Exception e) {
                messageLabel.setText(" [ Error al cargar imagen ] ");
                messageLabel.setForeground(Color.RED);
            }
        } else {
            messageLabel.setText(msg.getBody());
            messageLabel.setForeground(Color.BLACK);
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        // Metadata
        String metaText = msg.getDate() != null ? msg.getDate() : "";

        if (isMine && msg.getStatusMessage() != null) {
            String statusMark = "";
            switch (msg.getStatusMessage()) {
                case SENT: statusMark = "  Enviado"; break;
                case RECEIVED: statusMark = "  Leido"; break;
                default: break;
            }
            metaText += statusMark;
        }
        metaLabel.setText(metaText);

        // Ensamblado
        bubblePanel.add(messageLabel, BorderLayout.CENTER);
        bubblePanel.add(metaLabel, BorderLayout.SOUTH);

        if (isMine) {
            panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        } else {
            panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        }

        panel.add(bubblePanel);

        return panel;
    }
}