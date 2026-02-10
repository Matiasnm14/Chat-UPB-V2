package DB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatUI extends JFrame {

    private JTextField txtIp;
    private JTextField txtPort;
    private JButton btnConnect;
    private JButton btnDisconnect;

    private JTextArea txtChat;
    private JTextField txtMessage;
    private JButton btnSend;

    private JLabel lblStatus;

    private SocketClient client;

    public ChatUI() {
        setTitle("ChatUPB - Cliente");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);

        // mi head
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(750, 60));
        header.setBackground(new Color(0, 51, 102));

        JLabel lblChat = new JLabel("  CHAT ");
        lblChat.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblChat.setForeground(Color.WHITE);

        JLabel lblUpb = new JLabel("UPB");
        lblUpb.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblUpb.setForeground(new Color(255, 204, 0));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 14));
        titlePanel.setOpaque(false);
        titlePanel.add(lblChat);
        titlePanel.add(lblUpb);

        JPanel connPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 14));
        connPanel.setOpaque(false);

        JLabel lblIp = new JLabel("IP:");
        lblIp.setForeground(Color.WHITE);

        JLabel lblPort = new JLabel("Puerto:");
        lblPort.setForeground(Color.WHITE);

        txtIp = new JTextField(10);
        txtPort = new JTextField( 5);


        btnConnect = new JButton("Conectar");
        btnDisconnect = new JButton("Desconectar");
        btnDisconnect.setEnabled(false);

        connPanel.add(lblIp);
        connPanel.add(txtIp);
        connPanel.add(lblPort);
        connPanel.add(txtPort);
        connPanel.add(btnConnect);
        connPanel.add(btnDisconnect);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(connPanel, BorderLayout.EAST);

        // chat
        txtChat = new JTextArea();
        txtChat.setEditable(false);
        txtChat.setLineWrap(true);
        txtChat.setWrapStyleWord(true);
        txtChat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtChat.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txtChat);


        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(new EmptyBorder(10, 10, 10, 10));

        txtMessage = new JTextField();
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnSend = new JButton("Enviar");
        btnSend.setEnabled(false);

        bottom.add(txtMessage, BorderLayout.CENTER);
        bottom.add(btnSend, BorderLayout.EAST);

        // estado
        lblStatus = new JLabel("Estado: Desconectado");
        lblStatus.setBorder(new EmptyBorder(6, 10, 6, 10));


        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(bottom, BorderLayout.CENTER);
        south.add(lblStatus, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);


        btnConnect.addActionListener(e -> connect());
        btnDisconnect.addActionListener(e -> disconnect());

        btnSend.addActionListener(e -> sendMessage());
        txtMessage.addActionListener(e -> sendMessage());

    }

    private void connect() {
        try {
            String ip = txtIp.getText().trim();

            //client = new SocketClient(ip);

            client.setOnMessage(msg -> appendChat("SERVER: " + msg));
            client.setOnDisconnect(() -> appendChat("** Desconectado **"));

            client.start();

            lblStatus.setText("Estado: Conectado a " + ip + ":3000");
            btnConnect.setEnabled(false);
            btnDisconnect.setEnabled(true);
            btnSend.setEnabled(true);

            appendChat("** Conectado **");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnect() {
        try {
            if (client != null) client.close();
        } catch (Exception ignored) {}

        lblStatus.setText("Estado: Desconectado");
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        btnSend.setEnabled(false);

        appendChat("** Cerraste la conexión **");
    }

    private void sendMessage() {
        String msg = txtMessage.getText().trim();
        if (msg.isEmpty()) return;

        try {
            client.send(msg + System.lineSeparator());
            appendChat("YO: " + msg);
            txtMessage.setText("");
            txtMessage.requestFocus();
        } catch (Exception ex) {
            appendChat("ERROR al enviar: " + ex.getMessage());
        }
    }

    private void appendChat(String line) {
        SwingUtilities.invokeLater(() -> {
            txtChat.append(line + "\n");
            txtChat.setCaretPosition(txtChat.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatUI().setVisible(true));
    }
}
