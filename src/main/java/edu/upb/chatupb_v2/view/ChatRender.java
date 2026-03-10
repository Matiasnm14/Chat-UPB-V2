package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class ChatRender extends JPanel implements ListCellRenderer<Message> {

    private JLabel messageLabel;
    private JPanel container;

    private JPanel bubblePanel;
    private JLabel metaLabel;
    public ChatRender() {
        setLayout(new BorderLayout());
        setOpaque(false); // Transparente para no tapar el fondo de la lista

        // Configuramos la etiqueta que tendrá el color sólido
        messageLabel = new JLabel();
        messageLabel.setOpaque(false); // Obligatorio para que se vea el background
        messageLabel.setBorder(new EmptyBorder(8, 12, 8, 12)); // Un poco de espacio interior
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        metaLabel = new JLabel();
        metaLabel.setFont(new Font("Arial", Font.ITALIC, 10)); // Letra más pequeña
        metaLabel.setForeground(Color.GRAY); // Color sutil

        bubblePanel = new JPanel();
        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setOpaque(true); // Ahora el color de fondo irá aquí
        bubblePanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Contenedor con FlowLayout para controlar izquierda/derecha
        bubblePanel.add(messageLabel, BorderLayout.CENTER);
        bubblePanel.add(metaLabel, BorderLayout.SOUTH);
        container = new JPanel(new FlowLayout());
        container.setOpaque(false);
        container.add(bubblePanel);

        add(container, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message msg, int index, boolean isSelected, boolean cellHasFocus) {

        // Ponemos el texto del mensaje
        messageLabel.setText("<html><p style='width: 250px;'>" + msg.getBody() + "</p></html>");

        boolean isMine = msg.getStatusMessage() != StatusMessage.READ;

        // Evaluamos de quién es el mensaje para cambiar color y posición
        if (isMine) {
            // MIS MENSAJES: Derecha y color verde sólido
            ((FlowLayout) container.getLayout()).setAlignment(FlowLayout.RIGHT);
            bubblePanel.setBackground(new Color(220, 248, 198));
            String state = msg.getStatusMessage() == StatusMessage.RECEIVED? "Leido" : "Enviado";
            metaLabel.setText(msg.getDate() + " " + state);
        } else {
            // MENSAJES DEL CONTACTO: Izquierda y color gris sólido
            ((FlowLayout) container.getLayout()).setAlignment(FlowLayout.LEFT);
            bubblePanel.setBackground(new Color(240, 240, 240));
            metaLabel.setText(msg.getDate());
        }

        metaLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        return this;
    }



}
