package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Controller.*;
import edu.upb.chatupb_v2.Model.entities.*;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
public class JUi extends JFrame implements IChatView {
    @Setter
    @Getter
    private UIController UIController;
    private String username;
    private final UUID userId = UUID.fromString("557e37e7-4853-4136-aae5-fce08e133272");

    private User selectedUser;
    private DefaultListModel<User> chatListModel;
    private JList<User> chatList;

    public void setController(ContactController controller) {
        this.controller = controller;
        renderContacts();
    }

    private ContactController controller;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;

    private JTextField jTextMensaje;
    private JLabel jOnline;

    public JUi() {
        this.username = askForUsername();
        if (this.username == null || this.username.trim().isEmpty()) {
            System.exit(0);
        }
        initComponents();
        this.UIController = new UIController(this, username, userId.toString());
    }

    private String askForUsername() {
        return JOptionPane.showInputDialog(
                this,
                "Ingresa tu nombre de usuario:",
                "Bienvenida a ChatUPB",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    private void initComponents() {
        setTitle("ChatUPB");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ================= LEFT PANEL =================
        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFixedCellHeight(60);
        chatList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        chatList.setBackground(new Color(240, 240, 240));

        JScrollPane leftScrollPane = new JScrollPane(chatList);
        leftScrollPane.setPreferredSize(new Dimension(250, 600));

        // ================= RIGHT PANEL =================
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        jTextMensaje = new JTextField();
        JButton btnSend = new JButton("Enviar");
        JButton btnBuzz = new JButton("Buzz");
        JButton btnOffline = new JButton("Fuera de Línea");
        JButton btnNewConnection = new JButton("Nueva Conexión");

        jOnline = new JLabel("Status: Online");

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnNewConnection);
        topPanel.add(btnBuzz);
        topPanel.add(btnOffline);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(jTextMensaje, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(leftScrollPane, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // ================= ACTIONS =================

        btnSend.addActionListener(e -> {
            String text = jTextMensaje.getText().trim();
            if (!text.isEmpty()) {
                addMessage(text, true);  // mensaje propio
                UIController.sendMessage(text, selectedUser);
                jTextMensaje.setText("");
            }
        });

        btnBuzz.addActionListener(e -> UIController.sendBuzz());
        btnOffline.addActionListener(e -> UIController.sendBye());

        btnNewConnection.addActionListener(e ->
                new ConnectionDialog(this, UIController).setVisible(true)
        );

        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User newSelected = chatList.getSelectedValue();
                if (newSelected != null) {
                    selectedUser = newSelected;
                    renderMessages(controller.returnMessages(this.userId.toString(), selectedUser.getId()));
                }
            }
        });
        JPopupMenu popup = new JPopupMenu();

        JMenuItem j1 = new JMenuItem("ACCIÓN A");
        JMenuItem j2 = new JMenuItem("ACCIÓN B");
        popup.add(j1);
        popup.add(j2);

        j1.addActionListener(e -> {
            User selected = chatList.getSelectedValue();
            System.out.println("USER: "+selected.getIp());
            UIController.connectPrev(selected);
        });

        j2.addActionListener(e -> {
            String selected = chatList.getSelectedValue().toString();
            System.out.println("ACCIÓN B AND" + selected);
        });

        chatList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)){
                    int index = chatList.locationToIndex(e.getPoint());
                    chatList.setSelectedIndex(index);
                    popup.show(chatList, e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }


    public void init() {
        EventQueue.invokeLater(() -> setVisible(true));
    }

    private void addMessage(String text, boolean isOwnMessage) {
        MessageBubble bubble = new MessageBubble(text, isOwnMessage);
        messagesPanel.add(bubble);
        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    // ================= IChatView =================

    @Override
    public void updateStatus(String status) {
        jOnline.setText(status);
    }

    @Override
    public void showMessage(String message) {
        addMessage(message, false); // mensaje recibido
    }

    @Override
    public void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public boolean showInvitationDialog(String userName, String id) {
        int res = JOptionPane.showConfirmDialog(
                this,
                "Invitación de: " + userName + ". ¿Aceptar?",
                "Nueva conexión",
                JOptionPane.YES_NO_OPTION
        );
        return res == JOptionPane.YES_OPTION;
    }

    @Override
    public void showBuzzNotification(String senderName) {
        JOptionPane.showMessageDialog(this,
                senderName + " te envió un buzz",
                "Buzz",
                JOptionPane.INFORMATION_MESSAGE);
    }


    //TODO
    @Override
    public void showChat(Chat chat) {

        String name = ClientController.getInstance()
                .getClients()
                .get(chat.getSendUser())
                .getNombre();

        addMessage(name + ": " + chat.getMessage(), false);
    }

    @Override
    public void renderContacts() {
        chatListModel.clear();
        try {
            List<User> users = controller.returnContacts();
            for (User user : users) {
                chatListModel.addElement(user);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    @Override
    public void renderMessages(List<Message> messages) {
        messagesPanel.removeAll();

        for (Message message : messages) {
            boolean isOwn = message.getSendUser().equals(userId.toString());
            MessageBubble bubble = new MessageBubble(message.getBody(), isOwn);
            messagesPanel.add(bubble);
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    @Override
    public void showByeNotification(String id) {
        JOptionPane.showMessageDialog(this,
                id + " se desconectó",
                "Desconectado",
                JOptionPane.INFORMATION_MESSAGE);
    }
}