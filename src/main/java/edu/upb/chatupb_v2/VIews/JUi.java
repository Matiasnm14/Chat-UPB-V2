package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Controller.ChatService;
import edu.upb.chatupb_v2.Controller.Controller;
import edu.upb.chatupb_v2.Model.network.SocketClient;
import edu.upb.chatupb_v2.Model.entities.User;
import edu.upb.chatupb_v2.Model.repository.UserDAO;
import edu.upb.chatupb_v2.Model.entities.comands.Chat;
import edu.upb.chatupb_v2.Model.entities.comands.Invitation;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class JUi extends JFrame implements IChatView {

    private ChatService chatService;
    private String username;
    private final UUID userId = UUID.randomUUID();
    private static final Logger logger = Logger.getLogger(JUi.class.getName());

    private DefaultListModel<User> chatListModel;
    private JList<User> chatList;

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
        this.chatService = new ChatService(this, username, userId.toString());
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
                chatService.sendMessage(text);
                jTextMensaje.setText("");
            }
        });

        btnBuzz.addActionListener(e -> chatService.sendBuzz());
        btnOffline.addActionListener(e -> chatService.sendBye());

        btnNewConnection.addActionListener(e ->
                new ConnectionDialog(this, chatService).setVisible(true)
        );

        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selectedOne = chatList.getSelectedValue();
                if (selectedOne != null) {
                    try {
                        SocketClient cs = new SocketClient(selectedOne.getIp());
                        Invitation inv = new Invitation(this.userId.toString(), this.username);
                        cs.send(inv.createFormat());
                    } catch (IOException ex) {
                        showError("Error conectando con el usuario.");
                    }
                }
            }
        });

        renderContacts();
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

    @Override
    public void showChat(Chat chat) {
        String name = Controller.getInstance()
                .getClients()
                .get(chat.getIdUser())
                .getNombre();

        addMessage(name + ": " + chat.getMessage(), false);
    }

    @Override
    public void renderContacts() {
        chatListModel.clear();
        java.util.List<User> users = new ArrayList<>();
        try {
            users = UserDAO.getInstance().findAll();
        } catch (SQLException | ConnectException e) {
            logger.warning(e.getMessage());
        }

        for (User user : users) {
            chatListModel.addElement(user);
        }
    }

    @Override
    public void showByeNotification(String id) {
        JOptionPane.showMessageDialog(this,
                id + " se desconectó",
                "Desconectado",
                JOptionPane.INFORMATION_MESSAGE);
    }
}