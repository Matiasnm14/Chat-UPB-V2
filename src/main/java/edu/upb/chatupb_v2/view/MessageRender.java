package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MessageRender extends JPanel implements ListCellRenderer<Message> {
    private JPanel bubblePanel;
    private JLabel textLabel;
    private JLabel timeLabel;


    public MessageRender() {
        setLayout(new BorderLayout());
        setOpaque(false);

        bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar el globo redondeado
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        textLabel = new JLabel();
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        bubblePanel.add(textLabel, BorderLayout.CENTER);
        bubblePanel.add(timeLabel, BorderLayout.SOUTH);
    }


    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message msg, int index, boolean isSelected, boolean cellHasFocus) {
        removeAll();


        boolean isMe = !msg.getStatusMessage().toString().equals("RECEIVED");

        if (isMe) {
            bubblePanel.setBackground(new Color(220, 248, 198));
            textLabel.setForeground(Color.BLACK);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            wrapper.setOpaque(false);
            wrapper.add(bubblePanel);
            add(wrapper, BorderLayout.CENTER);

            timeLabel.setText("Yo - " + msg.getStatusMessage().toString());
            timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        } else {
            bubblePanel.setBackground(Color.WHITE);
            textLabel.setForeground(Color.BLACK);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            wrapper.setOpaque(false);
            wrapper.add(bubblePanel);
            add(wrapper, BorderLayout.CENTER);

            timeLabel.setText(msg.getDate());
            timeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        }

        textLabel.setText("<html><p style='width: 200px;'>" + msg.getBody() + "</p></html>");

        if (isSelected) {
            setBackground(new Color(230, 230, 230));
            setOpaque(true);
        } else {
            setOpaque(false);
        }

        return this;
    }
}
