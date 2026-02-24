/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.repository.comands.*;
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
    private final List<SocketListener> listeners = new ArrayList<>();


//    @Getter
//    private final Map<String, SocketListener> listener = new HashMap<>();


    public String getNombre() {
        return name;
    }

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        addListener(new MediatorListener());
    }

    public SocketClient(String ip) throws IOException {
        this.socket = new Socket(ip, 1900);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        addListener(new MediatorListener());
    }


    //ALGO
    public abstract static class SocketListener {
        public void onInvitationReceived(Invitation invitation) {}
        public void onAcceptReceived(Accept accept) {}
        public void onDeclineReceived(Decline decline) {}
        public void onHelloReceived(Hello hello) {}
        public void onAcceptHelloReceived(AcceptHello acceptHello) {}
        public void onDeclineHelloReceived(DeclineHello declineHello) {}
        public void onChatReceived(Chat chat) {}
        public void onConfirmedReceived(ConfirmRecived confirmRecived) {}
        public void onDeleteMessageReceived(DeleteMessage deleteMessage) {}
        public void onBuzzingReceived(Buzzing buzzing) {}
        public void onPinMessageReceived(PinMessage pinMessage) {}
        public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {}
        public void onThemeReceived(Theme theme) {}
        public void onGoodByeReceived(GoodBye goodBye) {}
    }

    private static class MediatorListener extends SocketListener {
        @Override
        public void onInvitationReceived(Invitation invitation) {
            java.awt.EventQueue.invokeLater(() -> Mediator.getInstance().onInvitationReceived(invitation, invitation.getIdUser()));
        }

        @Override
        public void onAcceptReceived(Accept accept) {
            java.awt.EventQueue.invokeLater(() -> Mediator.getInstance().onAcceptReceived(accept, accept.getIdUser()));
        }

        @Override
        public void onDeclineReceived(Decline decline) {
            java.awt.EventQueue.invokeLater(() -> Mediator.getInstance().onDeclineReceived(decline, decline.getIdUser()));
        }

        @Override
        public void onHelloReceived(Hello hello) {
            Mediator.getInstance().onHelloReceived(hello, hello.getIdUser());
        }

        @Override
        public void onAcceptHelloReceived(AcceptHello acceptHello) {
            java.awt.EventQueue.invokeLater(() -> Mediator.getInstance().onAcceptHelloReceived(acceptHello));
        }

        @Override
        public void onChatReceived(Chat chat) {
            java.awt.EventQueue.invokeLater(() -> Mediator.getInstance().onChatReceived(chat, chat.getIdUser()));
        }

        @Override
        public void onConfirmedReceived(ConfirmRecived confirmRecived) {
            Mediator.getInstance().onConfirmedReceived(confirmRecived);
        }

        @Override
        public void onBuzzingReceived(Buzzing buzzing) {
            java.awt.EventQueue.invokeLater(() -> Mediator.getInstance().onBuzzingReceived(buzzing, buzzing.getIdUser()));
        }

        @Override
        public void onGoodByeReceived(GoodBye goodBye) {
            java.awt.EventQueue.invokeLater(() -> Mediator.getInstance().onGoodByeReceived(goodBye, goodBye.getIdUser()));
        }
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
            String message;
            while ((message = br.readLine()) != null) {
                String[] split = message.split(Pattern.quote("|"));
                if (split.length == 0) continue;


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
                        for (SocketListener listener : listeners) {
                            listener.onHelloReceived(hel);
                        }

                        break;
                    }
                    case "005": {
                        AcceptHello acpHel = AcceptHello.parse(message);
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
                    case "0018": {
                        GoodBye gb = GoodBye.parse(message);
                        for (SocketListener listener : listeners) {
                            listener.onGoodByeReceived(gb);
                        }
                    }
                }
            }
        } catch (SocketException socketException) {
            System.out.println("Socket cerrado ");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    public void send(String message) throws IOException {
        try {
            dout.write(message.getBytes(StandardCharsets.UTF_8));
            dout.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
        }
    }
}
