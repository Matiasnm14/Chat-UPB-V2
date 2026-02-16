package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.server.Controller;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.repository.comands.*;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.SocketException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JUi extends JFrame {

    private static final Logger logger = Logger.getLogger(JUi.class.getName());
    private final String username = "Santiago";
    private final UUID userId = UUID.randomUUID();

    // Componentes de UI
    private JTextField txtIPAddress;
    private JTextField txtUserName;
    private JTextField txtMessage;
    private JLabel lblStatus;
    private JButton btnConnect;
    private JButton btnSend;
    private JButton btnBuzz;

    private SocketClient socketClient;

    public JUi() {
        setupAppearance();
        initComponents();
        startHeartbeatThread();
    }

    private void setupAppearance() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to set Look and Feel", ex);
        }
    }

    private void initComponents() {
        setTitle("Chat UPB - " + username);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Inicialización de componentes
        txtIPAddress = new JTextField("127.0.0.1");
        txtUserName = new JTextField(username);
        txtMessage = new JTextField();
        lblStatus = new JLabel("Status: Offline");
        lblStatus.setForeground(Color.GRAY);

        btnConnect = new JButton("Conectar");
        btnSend = new JButton("Enviar");
        btnBuzz = new JButton("Zumbido");

        // Layout
        setLayout(new BorderLayout(10, 10));

        // Panel Norte: Conexión
        JPanel panelNorth = new JPanel(new GridLayout(2, 3, 5, 5));
        panelNorth.add(new JLabel("Dirección IP:"));
        panelNorth.add(new JLabel("Usuario:"));
        panelNorth.add(new JLabel("")); // Placeholder
        panelNorth.add(txtIPAddress);
        panelNorth.add(txtUserName);
        panelNorth.add(btnConnect);

        // Panel Central: Acciones y Estado
        JPanel panelCenter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCenter.add(lblStatus);
        panelCenter.add(btnBuzz);

        // Panel Sur: Mensajería
        JPanel panelSouth = new JPanel(new BorderLayout(5, 5));
        panelSouth.add(txtMessage, BorderLayout.CENTER);
        panelSouth.add(btnSend, BorderLayout.EAST);

        // Añadir al Frame con márgenes
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(panelNorth, BorderLayout.NORTH);
        mainPanel.add(panelCenter, BorderLayout.CENTER);
        mainPanel.add(panelSouth, BorderLayout.SOUTH);

        add(mainPanel);

        // Listeners
        btnConnect.addActionListener(e -> handleConnect());
        btnSend.addActionListener(e -> handleSendMessage());
        btnBuzz.addActionListener(e -> handleSendBuzz());

        pack();
        setLocationRelativeTo(null);
    }

    // --- Lógica de Eventos ---

    private void handleConnect() {
        String ip = txtIPAddress.getText();
        new Thread(() -> {
            try {
                updateStatus("Conectando...", Color.ORANGE);
                socketClient = new SocketClient(ip);
                socketClient.setListener(username, userId.toString(), connectionListener);
                Controller.addClients(socketClient);
                socketClient.start();

                Invitation myInvite = new Invitation(userId.toString(), username);
                socketClient.send(myInvite.createFormat());
            } catch (Exception e) {
                showError("Error de conexión: " + e.getMessage());
                updateStatus("Error", Color.RED);
            }
        }).start();
    }

    private void handleSendMessage() {
        String text = txtMessage.getText();
        if (text.isEmpty()) return;

        try {
            Chat chat = new Chat(userId.toString(), UUID.randomUUID().toString(), text);
            sendData(chat.createFormat());
            txtMessage.setText("");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al enviar mensaje", e);
        }
    }

    private void handleSendBuzz() {
        Buzzing bz = new Buzzing(userId.toString());
        sendData(bz.createFormat());
    }

    // --- Utilidades ---

    private void sendData(String data) {
        for (SocketClient sc : Controller.getClients()) {
            try {
                sc.send(data);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error broadcasting to client", e);
            }
        }
    }

    private void updateStatus(String status, Color color) {
        SwingUtilities.invokeLater(() -> {
            lblStatus.setText("Status: " + status);
            lblStatus.setForeground(color);
        });
    }

    private void showError(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void startHeartbeatThread() {
        Thread hbThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    Hello hello = new Hello(userId.toString());
                    sendData(hello.createFormat());
                } catch (Exception e) {
                    logger.log(Level.FINE, "Heartbeat interrupted");
                }
            }
        });
        hbThread.setDaemon(true);
        hbThread.start();
    }

    // --- Listener de Sockets ---

    @Getter
    private final SocketClient.SocketListener connectionListener = new SocketClient.SocketListener() {
        @Override
        public void onInvitationReceived(Invitation invitation) {
            SwingUtilities.invokeLater(() -> {
                int resp = JOptionPane.showConfirmDialog(JUi.this,
                        "Invitación de: " + invitation.getUserName(), "Nueva Conexión",
                        JOptionPane.YES_NO_OPTION);

                try {
                    if (resp == JOptionPane.YES_OPTION) {
                        Accept acp = new Accept(userId.toString(), username);
                        for (SocketClient sc : Controller.getClients()) {
                            if (sc.getUID().equals(invitation.getIdUser())) sc.send(acp.createFormat());
                        }
                    } else {
                        socketClient.send(new Decline().createFormat());
                    }
                } catch (SocketException s){
                    System.out.println("Socket Cerrado");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error responding to invitation", e);
                }
            });
        }

        @Override
        public void onAcceptReceived(Accept accept) {
            updateStatus("Online", new Color(0, 150, 0));
        }

        @Override
        public void onDeclineReceived(Decline decline) {
            updateStatus("Rejected", Color.RED);
            socketClient.close();
        }

        @Override
        public void onChatReceived(Chat chat) {
            System.out.println("Mensaje de " + chat.getIdUser() + ": " + chat.getMessage());
            ConfirmRecived confirm = new ConfirmRecived(chat.getIdMessage());
            sendData(confirm.createFormat());
        }

        @Override
        public void onBuzzingReceived(Buzzing buzzing) {
            String sender = Controller.getClients().stream()
                    .filter(c -> c.getUID().equals(buzzing.getIdUser()))
                    .map(SocketClient::getNombre)
                    .findFirst().orElse("Alguien");

            JOptionPane.showMessageDialog(JUi.this, sender + " te envió un zumbido!");
        }

        // Métodos vacíos simplificados para brevedad
        @Override public void onHelloReceived(Hello h) {
            try { sendData(new AcceptHello(userId.toString()).createFormat()); } catch (Exception e){}
        }
        @Override public void onAcceptHelloReceived(AcceptHello a) { updateStatus("Online", new Color(0, 150, 0)); }
        @Override public void onDeclineHelloReceived(DeclineHello d) { updateStatus("Offline", Color.GRAY); }
        @Override public void onConfirmedReceived(ConfirmRecived c) { logger.info("Mensaje confirmado"); }
        @Override public void onDeleteMessageReceived(DeleteMessage d) {}
        @Override public void onPinMessageReceived(PinMessage p) {}
        @Override public void onUniqueMessageReceived(UniqueMessage u) {}
        @Override public void onThemeReceived(Theme t) {}
    };

    public void init () {
        SwingUtilities.invokeLater(() -> new JUi().setVisible(true));
    }
}