package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.repository.comands.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Pattern;

public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private SocketListener listener;

    // Constructor cuando el servidor acepta conexión
    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        this.dout = new DataOutputStream(socket.getOutputStream());
        this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    // Constructor original (usa puerto 1900)
    public SocketClient(String ip) throws IOException {
        this(ip, 1900);
    }

    // NUEVO constructor con puerto variable
    public SocketClient(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        this.ip = ip;
        this.dout = new DataOutputStream(socket.getOutputStream());
        this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }
    public interface SocketListener {
        void onInvitationReceived(Invitation invitation);
        void onAcceptReceived(Accept accept);
        void onDeclineReceived(Decline decline);
        void onHelloReceived(Hello hello);
        void onAcceptHelloReceived(AcceptHello acceptHello);
        void onDeclineHelloReceived(DeclineHello declineHello);
        void onChatReceived(Chat chat);
        void onConfirmedReceived(ConfirmRecived confirmRecived);
        void onDeleteMessageReceived(DeleteMessage deleteMessage);
        void onBuzzingReceived(Buzzing buzzing);
        void onPinMessageReceived(PinMessage pinMessage);
        void onUniqueMessageReceived(UniqueMessage uniqueMessage);
        void onThemeReceived(Theme theme);
    }

    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            String message;

            while ((message = br.readLine()) != null) {

                System.out.println("Mensaje recibido: " + message);

                String[] split = message.split(Pattern.quote("|"));
                if (split.length == 0) continue;

                switch (split[0]) {

                    case "001": {
                        Invitation inv = Invitation.parse(message);
                        if (listener != null) listener.onInvitationReceived(inv);
                        break;
                    }

                    case "002": {
                        Accept acp = Accept.parse(message);
                        if (listener != null) listener.onAcceptReceived(acp);
                        break;
                    }

                    case "003": {
                        Decline dec = Decline.parse(message);
                        if (listener != null) listener.onDeclineReceived(dec);
                        break;
                    }

                    case "004": {
                        Hello hel = Hello.parse(message);
                        if (listener != null) listener.onHelloReceived(hel);
                        break;
                    }

                    case "005": {
                        AcceptHello acpHel = AcceptHello.parse(message);
                        if (listener != null) listener.onAcceptHelloReceived(acpHel);
                        break;
                    }

                    case "006": {
                        DeclineHello decHel = DeclineHello.parse(message);
                        if (listener != null) listener.onDeclineHelloReceived(decHel);
                        break;
                    }

                    case "007": {
                        Chat cht = Chat.parse(message);
                        if (listener != null) listener.onChatReceived(cht);
                        break;
                    }

                    case "008": {
                        ConfirmRecived conRec = ConfirmRecived.parse(message);
                        if (listener != null) listener.onConfirmedReceived(conRec);
                        break;
                    }

                    case "009": {
                        DeleteMessage delMes = DeleteMessage.parse(message);
                        if (listener != null) listener.onDeleteMessageReceived(delMes);
                        break;
                    }

                    case "010": {
                        Buzzing buz = Buzzing.parse(message);
                        if (listener != null) listener.onBuzzingReceived(buz);
                        break;
                    }

                    case "011": {
                        PinMessage pinMes = PinMessage.parse(message);
                        if (listener != null) listener.onPinMessageReceived(pinMes);
                        break;
                    }

                    case "012": {
                        UniqueMessage uniMes = UniqueMessage.parse(message);
                        if (listener != null) listener.onUniqueMessageReceived(uniMes);
                        break;
                    }

                    case "013": {
                        Theme thm = Theme.parse(message);
                        if (listener != null) listener.onThemeReceived(thm);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Conexión cerrada con: " + ip);
        }
    }

    // Enviar mensaje
    public void send(String message) throws IOException {
        dout.write((message + System.lineSeparator()).getBytes("UTF-8"));
        dout.flush();
    }

    // Cerrar conexión
    public void close() {
        try {
            if (socket != null) socket.close();
            if (br != null) br.close();
            if (dout != null) dout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
