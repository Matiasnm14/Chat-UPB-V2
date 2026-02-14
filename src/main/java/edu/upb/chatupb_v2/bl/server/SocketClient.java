/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.repository.comands.*;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    @Getter
    private List<SocketListener> listener = new ArrayList<>();

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
    }

    public void setListener(SocketListener listener) {
        this.listener.add(listener);
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                System.out.println("Mensaje recibido: " + message);

                String split[] = message.split(Pattern.quote("|"));
                if(split.length == 0) continue;

                switch (split[0]) {
                    case "001": {
                        Invitation inv = Invitation.parse(message);
                        for (SocketListener socketListener : listener) {
                            java.awt.EventQueue.invokeLater(() -> {socketListener.onInvitationReceived(inv);});
                        }
                        break;
                    }
                    case "002": {
                        Accept acp = Accept.parse(message);
                        for (SocketListener socketListener: listener){
                            java.awt.EventQueue.invokeLater(() -> {socketListener.onAcceptReceived(acp);});
                        }
                        break;
                    }
                    case "003": {
                        Decline dec = Decline.parse(message);
                        for (SocketListener socketListener: listener){
                            java.awt.EventQueue.invokeLater(() -> {socketListener.onDeclineReceived(dec);});
                        }
                        break;
                    }
                    case "004": {
                        Hello hel = Hello.parse(message);
                        break;
                    }
                    case "005": {
                        AcceptHello acpHel = AcceptHello.parse(message);
                        break;
                    }
                    case "006": {
                        DeclineHello decHel = DeclineHello.parse(message);
                        break;
                    }
                    case "007": {
                        Chat cht = Chat.parse(message);
                        break;
                    }
                    case "008": {
                        ConfirmRecived conRec = ConfirmRecived.parse(message);
                        break;
                    }
                    case "009": {
                        DeleteMessage delMes = DeleteMessage.parse(message);
                        break;
                    }
                    case "010": {
                        Buzzing buz = Buzzing.parse(message);
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
                }
            }
        } catch (SocketException socketException){
            System.out.println("Zoquete digo socket cerrado ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void send(String message) throws IOException {
        try {
            dout.write(message.getBytes("UTF-8"));
            dout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.socket.close();
            this.br.close();
            this.dout.close();
        } catch (SocketException socketException) {
            System.out.println("Se ha cerrado el socket");
        } catch (Exception e){
            System.out.println(getAllStackTraces());;
        }
    }
}
