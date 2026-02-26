package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.model.entities.commands.*;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class SocketClient extends Thread {
    @Getter
    private final Map<String, SocketListener> listener = new HashMap<>();
    private final DataOutputStream dout;
    private final BufferedReader br;
    private final Socket socket;
    @Setter
    private String userName;
    @Setter
    private String uid;
    @Getter
    private final String ip;

    public String getUID() {
        return uid;
    }
    public String getNombre(){
        return userName;
    }

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public SocketClient(String ip) throws IOException {
        this.socket = new Socket(ip, 1900);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }


    //ALGO
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
        void onByeReceived(Bye bye);
    }

    public void setListener(String name, String key, SocketListener listener) {
        this.listener.put(key, listener);
        this.uid = key;
        this.userName = name;
    }

    private void fixClients(Command c) throws Exception {
        SocketListener sl = listener.get(uid);
        listener.remove(uid);
        if (c instanceof Invitation){
            this.userName = ((Invitation) c).getUserName();
            this.uid = ((Invitation)c).getIdUser();
        }
        else if (c instanceof Accept){
            this.userName = ((Accept) c).getUserName();
            this.uid = ((Accept) c).getIdUser();
        }
        listener.put(uid, sl);
        Controller.getInstance().addClients(this);
//        ContactDao.getInstance().save(new Contact(uid,name,ip));
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                String[] split = message.split(Pattern.quote("|"));
                if(split.length == 0) continue;
                System.out.println(message);
                switch (split[0]) {
                    case "001": {
                        Invitation inv = Invitation.parse(message);
                        fixClients(inv);

                        Controller.getInstance().notificarUI(inv);
                        break;
                    }
                    case "002": {
                        Accept acp = Accept.parse(message);
                        fixClients(acp);
                        Controller.getInstance().notificarUI(acp);
                        break;
                    }
                    case "003": {
                        Decline dec = Decline.parse(message);
                        Controller.getInstance().notificarUI(dec);
                        break;
                    }
                    case "004": {
                        Hello hel = Hello.parse(message);
                        Controller.getInstance().notificarUI(hel);
                        break;
                    }
                    case "005": {
                        AcceptHello acpHel = AcceptHello.parse(message);
                        Controller.getInstance().notificarUI(acpHel);
                        break;
                    }
                    case "006": {
                        DeclineHello decHel = DeclineHello.parse(message);
                        Controller.getInstance().notificarUI(decHel);
                        break;
                    }
                    case "007": {
                        Chat cht = Chat.parse(message);
                        Controller.getInstance().notificarUI(cht);
                        break;
                    }
                    case "008": {
                        ConfirmRecived conRec = ConfirmRecived.parse(message);
                        Controller.getInstance().notificarUI(conRec);
                        break;
                    }
                    case "009": {
                        DeleteMessage delMes = DeleteMessage.parse(message);
                        Controller.getInstance().notificarUI(delMes);
                        break;
                    }
                    case "010": {
                        Buzzing buz = Buzzing.parse(message);
                        Controller.getInstance().notificarUI(buz);
                        break;
                    }
                    case "011": {
                        PinMessage pinMes = PinMessage.parse(message);
                        break;
                    }
                    case "012": {
                        UniqueMessage uniMes = UniqueMessage.parse(message);
                        break;
                    }
                    case "013": {
                        Theme thm = Theme.parse(message);
                        break;
                    }
//                    case "0018": {
//                        Bye bye = Bye.parse(message);
//                        Controller.getInstance().notificarUI(bye);
//                        break;
//                    }
                }
            }
        } catch (SocketException socketException){
            System.out.println("Socket cerrado [Excepción de SocketClient]");
            this.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            System.out.println("SOCKET CLOSE: " +socketException.getMessage());
        } catch (Exception e){
            System.out.println(getAllStackTraces());
        }
    }
}
