/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.controller.Mediator;
import edu.upb.chatupb_v2.model.entities.comands.*;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;

    public String getUID() {
        return uid;
    }

    private String name;
    @Setter
    private String uid;
    @Getter
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private final Object sendLock = new Object();
    private final Object closeLock = new Object();
    private boolean closedNotified = false;
    private final List<SocketListener> listeners = new ArrayList<>();

    private static final String CODE_HELLO = "004";
    private static final String CODE_ACCEPT_HELLO = "005";

//    @Getter
//    private final Map<String, SocketListener> listener = new HashMap<>();


    public String getNombre() {
        return name;
    }

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
        addListener(Mediator.getInstance());
    }

    public SocketClient(String ip) throws IOException {
        this.socket = new Socket(ip, 1900);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
        addListener(Mediator.getInstance());
    }


    //ALGO
    public interface SocketListener {
        default void onInvitationReceived(Invitation invitation) {}
        default void onAcceptReceived(Accept accept) {}
        default void onDeclineReceived(Decline decline) {}
        default void onHelloReceived(Hello hello) {}
        default void onAcceptHelloReceived(AcceptHello acceptHello) {}
        default void onDeclineHelloReceived(DeclineHello declineHello) {}
        default void onChatReceived(Chat chat) {}
        default void onConfirmedReceived(ConfirmRecived confirmRecived) {}
        default void onDeleteMessageReceived(DeleteMessage deleteMessage) {}
        default void onBuzzingReceived(Buzzing buzzing) {}
        default void onPinMessageReceived(PinMessage pinMessage) {}
        default void onUniqueMessageReceived(UniqueMessage uniqueMessage) {}
        default void onThemeReceived(Theme theme) {}
        default void onContactShared(PasarContacto pasarContacto) {}
        default void onGoodByeReceived(GoodBye goodBye) {}
        default void onSocketClosed(SocketClient client) {}
    }

    public void addListener(SocketListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(SocketListener listener) {
        listeners.remove(listener);
    }

    public void setClient(String name, String key) {
//        this.listener.put(key, listener);
        this.uid = key;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            String raw;
            while ((raw = br.readLine()) != null) {
                String message = raw;
                String[] split = message.split(Pattern.quote("|"));
                if (split.length == 0) {
                    continue;
                }

                switch (split[0]) {
                    case "001": {
                        Invitation inv = Invitation.parse(message);
                        this.name = inv.getUserName();
                        this.uid = inv.getIdUser();
                        for (SocketListener listener : listeners) {
                            listener.onInvitationReceived(inv);
                        }

                        break;
                    }
                    case "002": {
                        Accept acp = Accept.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onAcceptReceived(acp);
                        }

                        break;
                    }
                    case "003": {
                        Decline dec = Decline.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onDeclineReceived(dec);
                        }

                        break;
                    }
                    case "004": {
                        Hello hel = Hello.parse(message);
                        logReceiveHello(CODE_HELLO, hel.getIdUser());
                        for (SocketListener listener : listeners) {
                            listener.onHelloReceived(hel);
                        }

                        break;
                    }
                    case "005": {
                        AcceptHello acpHel = AcceptHello.parse(message);
                        logReceiveHello(CODE_ACCEPT_HELLO, acpHel.getIdUser());
                        for (SocketListener listener : listeners) {
                            listener.onAcceptHelloReceived(acpHel);
                        }

                        break;
                    }
                    case "006": {
                        DeclineHello decHel = DeclineHello.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onDeclineHelloReceived(decHel);
                        }
                        break;
                    }
                    case "007": {
                        Chat cht = Chat.parse(message);

                        for (SocketListener listener : listeners) {
                            listener.onChatReceived(cht);
                        }

                        break;
                    }
                    case "008": {
                        ConfirmRecived conRec = ConfirmRecived.parse(message);

                        for (SocketListener listener : listeners) {
                            listener.onConfirmedReceived(conRec);
                        }

                        break;
                    }
                    case "009": {
                        DeleteMessage delMes = DeleteMessage.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onDeleteMessageReceived(delMes);
                        }
                        break;
                    }
                    case "010": {
                        Buzzing buz = Buzzing.parse(message);

                        for (SocketListener listener : listeners) {
                            listener.onBuzzingReceived(buz);
                        }

                        break;
                    }
                    case "011": {
                        PinMessage pinMes = PinMessage.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onPinMessageReceived(pinMes);
                        }
                        break;
                    }
                    case "012": {
                        UniqueMessage uniMes = UniqueMessage.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onUniqueMessageReceived(uniMes);
                        }
                        break;
                    }
                    case "013": {
                        Theme thm = Theme.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onThemeReceived(thm);
                        }
                        break;
                    }
                    case "020": {
                        PasarContacto pasarContacto = PasarContacto.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onContactShared(pasarContacto);
                        }
                        break;
                    }
                    case "0018": {
                        GoodBye gb = GoodBye.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onGoodByeReceived(gb);
                        }
                        break;
                    }
                }
            }
        } catch (SocketException socketException) {
            System.out.println("Socket cerrado ");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            notifyClosed();
        }
    }

    private void notifyClosed() {
        synchronized (closeLock) {
            if (closedNotified) {
                return;
            }
            closedNotified = true;
        }
        for (SocketListener listener : listeners) {
            listener.onSocketClosed(this);
        }
    }

    public void send(String message) throws IOException {
        if (message == null || message.isBlank()) {
            return;
        }
        String trimmed = trimLineEndings(message);
        logSendHelloIfNeeded(trimmed);
        sendPlain(trimmed);
    }

    public void close() {
        try {
            this.socket.close();
            this.br.close();
            this.dout.close();
        } catch (SocketException socketException) {
            System.out.println("Se ha cerrado el socket");
        } catch (Exception e) {
            System.out.println(getAllStackTraces());
        } finally {
            notifyClosed();
        }
    }

    private void sendPlain(String payload) throws IOException {
        String data = payload;
        if (!data.endsWith("\n") && !data.endsWith("\r\n")) {
            data += System.lineSeparator();
        }
        try {
            synchronized (sendLock) {
                dout.write(data.getBytes(StandardCharsets.UTF_8));
                dout.flush();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void logSendHelloIfNeeded(String payload) {
        String trimmed = trimLineEndings(payload);
        String[] parts = trimmed.split(Pattern.quote("|"));
        if (parts.length == 0) {
            return;
        }
        String code = parts[0];
        if (!CODE_HELLO.equals(code) && !CODE_ACCEPT_HELLO.equals(code)) {
            return;
        }
        String id = parts.length > 1 ? parts[1] : "";
        logSendHello(code, id);
    }

    private void logSendHello(String code, String id) {
        String label = CODE_HELLO.equals(code) ? "Hello" : "AcceptHello";
        System.out.println("Enviando " + code + " (" + label + ") a id " + safeId(id));
    }

    private void logReceiveHello(String code, String id) {
        String label = CODE_HELLO.equals(code) ? "Hello" : "AcceptHello";
        System.out.println("Recibido " + code + " (" + label + ") de id " + safeId(id));
    }

    private String safeId(String id) {
        if (id == null || id.isBlank()) {
            return "(sin id)";
        }
        return id;
    }

    private String trimLineEndings(String text) {
        if (text == null) {
            return "";
        }
        int end = text.length();
        while (end > 0) {
            char c = text.charAt(end - 1);
            if (c == '\n' || c == '\r') {
                end--;
            } else {
                break;
            }
        }
        return text.substring(0, end);
    }
}
