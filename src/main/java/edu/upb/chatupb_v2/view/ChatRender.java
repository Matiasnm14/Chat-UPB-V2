package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class ChatRender extends JPanel implements ListCellRenderer<Message> {

    private JLabel messageLabel;
    private JPanel container;

    public ChatRender() {
        setLayout(new BorderLayout());
        setOpaque(false); // Transparente para no tapar el fondo de la lista

        // Configuramos la etiqueta que tendrá el color sólido
        messageLabel = new JLabel();
        messageLabel.setOpaque(true); // Obligatorio para que se vea el background
        messageLabel.setBorder(new EmptyBorder(8, 12, 8, 12)); // Un poco de espacio interior
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Contenedor con FlowLayout para controlar izquierda/derecha
        container = new JPanel(new FlowLayout());
        container.setOpaque(false);
        container.add(messageLabel);

        add(container, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message msg, int index, boolean isSelected, boolean cellHasFocus) {

        // Ponemos el texto del mensaje
        messageLabel.setText(msg.getBody());

        boolean isMine = msg.getStatusMessage() != StatusMessage.READ;

        // Evaluamos de quién es el mensaje para cambiar color y posición
        if (isMine) {
            // MIS MENSAJES: Derecha y color verde sólido
            ((FlowLayout) container.getLayout()).setAlignment(FlowLayout.RIGHT);
            messageLabel.setBackground(new Color(220, 248, 198));
        } else {
            // MENSAJES DEL CONTACTO: Izquierda y color gris sólido
            ((FlowLayout) container.getLayout()).setAlignment(FlowLayout.LEFT);
            messageLabel.setBackground(new Color(240, 240, 240));
        }

        return this;
    }



}
