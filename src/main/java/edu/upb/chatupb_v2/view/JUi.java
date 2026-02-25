package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.controller.ChatService;
import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.controller.IChatView;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.repository.*;
import edu.upb.chatupb_v2.repository.*;
import edu.upb.chatupb_v2.model.entities.comands.Chat;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;
import java.util.logging.Logger;

public class JUi extends JFrame implements IChatView {

    private ChatService chatService;
    private String username;
    @Getter
    private String userId;
    private static final Logger logger = Logger.getLogger(JUi.class.getName());

    private JTextArea chatArea;
    private JTextField jTextMensaje;
    private JLabel jOnline;

    private DefaultListModel<Contact> chatListModel;
    private JList<Contact> chatList;

    // Añade esta variable para llevar el control del chat actual:
    private Contact currentContact;

    public JUi() {
        this.username = askForUsername();
        if (this.username == null || this.username.trim().isEmpty()) {
            System.exit(0);
        }
        initComponents();
        try {
            if (UserDao.getInstance().exist("name='"+username+"'"))
                userId = UserDao.getInstance().findByName(username).getId();
            else{
                userId = UUID.randomUUID().toString();
                UserDao.getInstance().save(new User(userId,username));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        this.chatService = new ChatService(this, username, userId);
        System.out.println(userId);
        Controller.getInstance().addUi(this);

        loadContacts();
    }
//    public void addModel(String contact){
//        if(!chatListModel.contains(contact)){
//            chatListModel.add(chatListModel.size(),contact);
//        }
//    }

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
        chatList.setFixedCellHeight(40); // Ajusta la altura si lo ves muy separado

        // ¡AQUÍ USAS TU RENDERER PERSONALIZADO!
        chatList.setCellRenderer(new ContactRender());


        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentContact = chatList.getSelectedValue();
                if (currentContact != null) {
                    loadMessages(currentContact.getId());
                }
            }
        });

        JScrollPane leftScrollPane = new JScrollPane(chatList);
        leftScrollPane.setPreferredSize(new Dimension(250, 600));



        // ================= RIGHT PANEL =================

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        jTextMensaje = new JTextField();
        JButton btnSend = new JButton("Enviar");

        JButton btnBuzz = new JButton("Buzz");
        JButton btnOffline = new JButton("Fuera de Línea");
        JButton btnNewConnection = new JButton("Nueva Conexión");

        jOnline = new JLabel("Status: Offline");

        // Top Panel (SOLO Buzz y Offline + Nueva Conexión)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnNewConnection);
        topPanel.add(btnBuzz);
        topPanel.add(btnOffline);

        // Bottom Panel (mensaje)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(jTextMensaje, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(leftScrollPane, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // ================= ACTIONS =================

        btnSend.addActionListener(e -> {
            String texto = jTextMensaje.getText().trim();

            if (!texto.isEmpty() && currentContact != null) {

                chatService.sendMessage(texto, currentContact.getId());
                jTextMensaje.setText("");
            } else if (currentContact == null) {
                showError("Por favor, selecciona un contacto de la lista izquierda para chatear.");
            }
        });

        btnBuzz.addActionListener(e -> chatService.sendBuzz());
        btnOffline.addActionListener(e -> chatService.sendBye());

        btnNewConnection.addActionListener(e ->
                new ConnectionDialog(this, chatService).setVisible(true)
        );
    }

    public void init() {
        EventQueue.invokeLater(() -> setVisible(true));
    }

    // ================= IChatView =================

    @Override
    public void updateStatus(String status) {
        jOnline.setText(status);
    }

    @Override
    public void showMessage(String message) {
        chatArea.append(message + "\n");
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
        String name = Controller.getInstance().getClients().get(chat.getIdUser()).getNombre();
        chatArea.append(name + " | " + chat.getMessage());
    }

    @Override
    public void showByeNotification(String id) {
        JOptionPane.showMessageDialog(this,
                id + " se desconectó",
                "Desconectado",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void addModel(Contact contact) {
        boolean exists = false;
        for (int i = 0; i < chatListModel.size(); i++) {
            if (chatListModel.get(i).getId().equals(contact.getId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            chatListModel.addElement(contact);
        }
    }


    // ================= DB LOADERS =================
    private void loadContacts() {
        try {
            chatListModel.clear();
            java.util.List<Contact> contacts = ContactDao.getInstance().findByOwner(this.userId);
            if (contacts != null) {
                for (Contact c : contacts) {
                    chatListModel.addElement(c);
                }
            }
        } catch (Exception e) {
            logger.severe("Error cargando contactos: " + e.getMessage());
        }
    }

    private void loadMessages(String contactId) {
        chatArea.setText("");
        try {
            java.util.List<Message> messages = MessageDAO.getInstance().findByContact(contactId);

            if (messages != null) {
                for (Message msg : messages) {
                    String senderName;
                    if (msg.getStatusMessage().toString().equals("SENT")) {
                        senderName = this.username;
                    } else {
                        senderName = currentContact.getName();
                    }
                    chatArea.append(senderName + " | " + msg.getBody() + "\n");
                }
            }
        } catch (Exception e) {
            logger.severe("Error cargando mensajes: " + e.getMessage());
        }
    }
}