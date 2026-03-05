package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.controller.UserController;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.entities.enums.Sound;
import edu.upb.chatupb_v2.model.repository.UserDao;
import edu.upb.chatupb_v2.model.entities.commands.Chat;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class JUi extends JFrame implements IChatView {

    private Controller controller;
    private ContactController contactController;
    private MessageController messageController;
    private UserController userController;
    private String username;
    @Getter
    private String userId;
    private static final Logger logger = Logger.getLogger(JUi.class.getName());

    private JTextArea chatArea;
    private JTextField jTextMensaje;
    private JLabel jOnline;

    private DefaultListModel<Contact> chatListModel;
    private JList<Contact> chatList;

    private Contact currentContact;

    public JUi(String name, String id) {


        this.userId = id;
        this.username = name;

        initComponents();

        this.controller = Controller.getInstance();
        controller.initController(username,userId,this);
        System.out.println(userId);

//
    }
//    private String askForUsername() {
//        return JOptionPane.showInputDialog(
//                this,
//                "Ingresa tu nombre de usuario:",
//                "Bienvenida a ChatUPB",
//                JOptionPane.QUESTION_MESSAGE
//        );
//    }

    private void initComponents() {
        setTitle("ChatUPB");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ================= LEFT PANEL =================

        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFixedCellHeight(40);

        chatList.setCellRenderer(new ContactRender());


        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentContact = chatList.getSelectedValue();
                if (currentContact != null) {
                    System.out.println("///////////Datos////////////");
                    System.out.println(currentContact.getId());
                    System.out.println(currentContact.getName());
                    System.out.println("//////////////////////////////");
                    messageController.onLoadMessages(currentContact.getId());

                    controller.markRead(currentContact);

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
        JButton btnConnect = new JButton("Conectar");


        jOnline = new JLabel("Status: Offline");

        // Top Panel (SOLO Buzz y Offline + Nueva Conexión)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnConnect);
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

                controller.sendMessage(texto, currentContact.getId());
                jTextMensaje.setText("");
            } else if (currentContact == null) {
                showError("Por favor, selecciona un contacto de la lista izquierda para chatear.");
            }
        });

        btnBuzz.addActionListener(e -> controller.sendBuzz());
        btnOffline.addActionListener(e -> controller.sendBye());

        btnNewConnection.addActionListener(e ->
                new ConnectionDialog(this, controller).setVisible(true)
        );
        btnConnect.addActionListener(e->{
            if(currentContact != null){
                System.out.println("CurrentContact: " + currentContact.getName());
                if(!controller.getClients().containsKey(currentContact.getId())){
                    controller.sendHello(currentContact.getIp(),currentContact.getName(),currentContact.getId());
                }

            }
        }
                );
    }

    public void init() {
        EventQueue.invokeLater(() -> setVisible(true));
    }

    private void shakeWindow() {
        final Point originalLocation = this.getLocation();
        final int shakeDistance = 15;

        Timer timer = new Timer(30, new java.awt.event.ActionListener() {
            int counter = 0;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (counter >= 20) {
                    setLocation(originalLocation);
                    ((Timer) e.getSource()).stop();
                } else {
                    int dx = (counter % 2 == 0) ? shakeDistance : -shakeDistance;
                    int dy = (counter % 4 < 2) ? shakeDistance : -shakeDistance;
                    setLocation(originalLocation.x + dx, originalLocation.y + dy);
                    counter++;
                }
            }
        });
        timer.start();
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
        showMessage("\n--- ¡" + senderName + " te ha enviado un Zumbido! ---\n");

        // 2. Reproducir el sonido usando nuestro Enum
        this.setExtendedState(javax.swing.JFrame.NORMAL);
        this.toFront();
        this.requestFocus();

        shakeWindow();
        Sound.BUZZ.play();
    }

    @Override
    public void showChat(Chat chat) {
        String name = Controller.getInstance().getClients().get(chat.getIdUser()).getNombre();
        chatArea.append(name + " | " + chat.getMessage()+ "\n");
    }

    @Override
    public void showByeNotification(String id) {
        JOptionPane.showMessageDialog(this,
                id + " se desconectó",
                "Desconectado",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onLoadContacts(List<Contact> contacts) {
        chatListModel.clear();

        if (contacts != null) {
            for (Contact c : contacts) {
                chatListModel.addElement(c);
            }
        }
    }

    @Override
    public void onLoadMessages(List<Message> messages) {
        chatArea.setText("");
        if (messages != null) {
            for (Message msg : messages) {
                String senderName;
                if (msg.getStatusMessage().toString().equals("RECEIVED")) {
                    senderName = currentContact.getName();
                    chatArea.append(senderName + " | " + msg.getBody() + "\n");
                } else {
                    senderName = "Tú";
                    if(msg.getStatusMessage().toString().equals("SENT")){
                        chatArea.append(senderName + " | " + msg.getBody() + " | ENVIADO" +"\n");
                    }else if (msg.getStatusMessage().toString().equals("READ")){
                        chatArea.append(senderName + " | " + msg.getBody() + " | LEIDO" +"\n");
                    }
                }
            }
        }
        chatArea.setAutoscrolls(false);
        chatArea.setAutoscrolls(true);
    }

    @Override
    public void onAddModel(Contact contact) {
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

    @Override
    public void refreshChatView() {
        if (currentContact != null) {
            messageController.onLoadMessages(currentContact.getId());
        }
    }

    @Override
    public Contact getCurrentContact() {
        return currentContact;
    }

    public void setContactController(ContactController contactController){
        this.contactController = contactController;
    }
    public void setMessageController(MessageController messageController){
        this.messageController = messageController;
    }
    public void setUserController(UserController userController){
        this.userController = userController;
    }
}