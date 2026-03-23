package edu.upb.chatupb_v2.Model.network;

import edu.upb.chatupb_v2.Controller.ClientController;
import edu.upb.chatupb_v2.Controller.exceptions.ChatException;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.factory.SocketListener;
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
    private final DataOutputStream dout;
    private final BufferedReader br;
    private final Socket socket;
    private String name;
    @Setter
    private String uid;
    @Getter
    private final String ip;

    @Getter
    @Setter
    private SocketListener socketListener;

    public String getUID() {
        return uid;
    }
    public String getNombre(){
        return name;
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

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                String[] split = message.split(Pattern.quote("|"));
                System.out.println(message);
                if(split.length == 0) continue;
                boolean b = (ClientController.getInstance().verificarBlock(this.uid));
                if (!b) {
                    switch (split[0]) {
                        case "001": {
                            Invitation inv = Invitation.parse(message);
                            this.uid = inv.getIdUser();
                            this.name = inv.getUserName();
                            socketListener.onInvitationReceived(inv, this);
                            break;
                        }
                        case "002": {
                            Accept acp = Accept.parse(message);
                            this.uid = acp.getIdUser();
                            this.name = acp.getUserName();
                            socketListener.onAcceptReceived(acp,this);
                            break;
                        }
                        case "003": {
                            Decline dec = Decline.parse(message);
                            socketListener.onDeclineReceived(dec);
                            break;
                        }
                        case "004": {
                            Hello hel = Hello.parse(message);
                            this.uid = hel.getIdUser();
                            socketListener.onHelloReceived(hel, this);
                            break;
                        }
                        case "005": {
                            AcceptHello acpHel = AcceptHello.parse(message);
                            socketListener.onAcceptHelloReceived(acpHel, this);
                            break;
                        }
                        case "006": {
                            DeclineHello decHel = DeclineHello.parse(message);
                            socketListener.onDeclineHelloReceived(decHel, this);
                            break;
                        }
                        case "007": {
                            Chat cht = Chat.parse(message);
                            socketListener.onChatReceived(cht);
                            break;
                        }
                        case "008": {
                            ConfirmRecived conRec = ConfirmRecived.parse(message);
                            socketListener.onConfirmedReceived(conRec);
                            break;
                        }
                        case "009": {
                            DeleteMessage delMes = DeleteMessage.parse(message);
                            socketListener.onDeleteMessageReceived(delMes);
                            break;
                        }
                        case "010": {
                            Buzzing buz = Buzzing.parse(message);
                            socketListener.onBuzzingReceived(buz);
                            break;
                        }
                        case "011": {
                            PinMessage pinMes = PinMessage.parse(message);
                            socketListener.onPinMessageReceived(pinMes);
                            break;
                        }
                        case "012": {
                            UniqueMessage uniMes = UniqueMessage.parse(message);
                            socketListener.onUniqueMessageReceived(uniMes);
                            break;
                        }
                        case "013": {
                            Theme thm = Theme.parse(message);
                            socketListener.onThemeReceived(thm);
                            break;
                        }
                        case "0018": {
                            Bye bye = Bye.parse(message);
                            socketListener.onByeReceived(bye);
                            break;
                        }
                        case "020": {
                            NewFriend nf = NewFriend.parse(message);
                            socketListener.onNewFriendReceived(nf);
                            break;
                        }
                        case "021": {
                            Image img = Image.parse(message);
                            socketListener.onImageReceived(img);
                            break;
                        }
                    }
                }

            }
        } catch (SocketException socketException){
            throw new ChatException(this);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String toString() {
        return "SocketClient{" +
                "dout=" + dout +
                ", br=" + br +
                ", socket=" + socket +
                ", name='" + name + '\'' +
                ", uid='" + uid + '\'' +
                ", ip='" + ip + '\'' +
                ", socketListener=" + socketListener +
                '}';
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