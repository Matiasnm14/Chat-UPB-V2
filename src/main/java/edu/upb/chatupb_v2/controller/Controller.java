package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.network.ChatServer;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;
import edu.upb.chatupb_v2.view.JUi;
import edu.upb.chatupb_v2.model.entities.comands.*;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
//import edu.upb.chatupb_v2.model.repository.comands.*;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class Controller implements SocketClient.SocketListener {
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    @Getter
    private Map<String, JUi> uis = new HashMap<>();
    private static Controller instance;
    @Getter
    private List<SocketClient> pendingClients = new ArrayList<>();
    @Setter
    private ChatServer server;

    public static Controller getInstance() {
        if (instance == null) instance = new Controller();
        return instance;
    }

    public void addClients(SocketClient client) {
        clients.putIfAbsent(client.getUID(), client);
    }

    public void delClients(String idUser) {
        clients.remove(idUser);
    }

    @Override
    public void notificarUI(Command command, String clientId) throws Exception {
        for (JUi sl : uis.values()) {
            if (command instanceof Invitation) {
//                SocketClient.SocketListener sl = clients.get(((Invitation) command).getIdUser()).getListener().get(((Invitation) command).getIdUser());
                sl.onInvitationReceived((Invitation) command);
            }
            if (command instanceof Accept) {

                sl.onAcceptReceived((Accept) command);
            }
            if (command instanceof Decline) {
                for (SocketClient sc : clients.values()) {

                    sl.onDeclineReceived((Decline) command);
                    SocketClient socketClient = clients.get(clientId);
                    delClients(socketClient.getUID());
                    if (socketClient != null) socketClient.close();
                }
            }
            if (command instanceof Hello) {
                for (SocketClient sc : clients.values()) {

                    sl.onHelloReceived((Hello) command);

                }
            }
            if (command instanceof AcceptHello) {

                sl.onAcceptHelloReceived((AcceptHello) command);
            }
            if (command instanceof DeclineHello) {
                for (SocketClient sc : clients.values()) {

                    sl.onDeclineHelloReceived((DeclineHello) command);

                }
            }

            if (command instanceof Chat) {

                sl.onChatReceived((Chat) command);

            }

            if (command instanceof ConfirmRecived) {
                for (SocketClient sc : clients.values()) {

                    sl.onConfirmedReceived((ConfirmRecived) command);

                }
            }

            if (command instanceof DeleteMessage) {
                String id_message = ((DeleteMessage) command).getIdMessage();
                MessageDAO.getInstance().delete(id_message);
            }

            if (command instanceof Buzzing) {

                sl.onBuzzingReceived((Buzzing) command);
            }


            if (command instanceof Bye) {
                for (SocketClient sc : clients.values()) {

                    sl.onByeReceived((Bye) command);

                }
            }
        }

    }

    public void addUi(JUi ui) {
        uis.putIfAbsent(ui.getUserId().toString(), ui);
    }
//    public void addContact(String contact){
//        for (JUi view : uis.values()){
//            view.addModel(contact);
//        }
//    }

    public void delUi(String idUi) {
        uis.remove(idUi);
    }



//    public void sendMessage(String texto, String idContacto){
//        if (clients.containsKey(idContacto)){
//            server.sendMessage(texto, idContacto);
//        }
//    }
//
//    public void sendBuzz(){
//        server.sendBuzz();
//    }
//
//    public void sendBye(){
//        server.sendBye();
//    }

    public void connect(String ip){
        new Thread(() -> {
            SocketClient socketClient;
            try {
                socketClient = new SocketClient(ip);
                socketClient.setListener(server.getUsername(), server.getUserId(), Controller.getInstance());

                socketClient.start();



//                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(server.getUserId()).
//                        updateStatus("Status: Enviando invitación..."));

            } catch (Exception e) {
                throw new OperationException("No se logró establecer la conexión");
//                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(server.getUserId())
//                        .showError("Error de conexión: " + e.getMessage()));
            }
            Invitation myInvite = new Invitation(server.getUserId(), server.getUsername());
            try {
                socketClient.send(myInvite.createFormat());
            } catch (IOException e) {
                throw new OperationException("No se logro enviar el mensaje");
            }

            pendingClients.add(socketClient);
        }).start();
    }

    public void sendMessage(String messageText, String destinationId) {
        if (destinationId == null || destinationId.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(server.getUserId()).
                    showError("Selecciona un contacto primero."));
            return;
        }

        try {
            Chat chat = new Chat(server.getUserId(), UUID.randomUUID().toString(), messageText);

            Message msgDb = new Message(
                    chat.getIdMessage(),
                    destinationId,
                    messageText,
                    TypeMessage.TEXT,
                    StatusMessage.SENT,
                    LocalDate.now().toString()
            );
            MessageDAO.getInstance().save(msgDb);

            SocketClient sc = Controller.getInstance().getClients().get(destinationId);

            if (sc != null) {
                sc.send(chat.createFormat());
                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(server.getUserId()).
                        showMessage("Tú | " + messageText));
            } else {
                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(server.getUserId()).
                        showError("El contacto no está en línea en este momento, pero el mensaje se guardó."));
            }

        } catch (Exception e) {
            throw new OperationException("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    public void sendBuzz() {
        for (SocketClient sc : Controller.getInstance().getClients().values()) {
            Buzzing bz = new Buzzing(this.server.getUserId());
            try {
                sc.send(bz.createFormat());
            } catch (IOException e) {
                throw new OperationException("No se pudo enviar el zumbido");
            }
        }
    }

    public void sendBye(){
        for (SocketClient sc : Controller.getInstance().getClients().values()) {
            Bye bye = new Bye(this.server.getUserId());
            try {
                sc.send(bye.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void startHelloService() {
        server.setHelloThread( new Thread(() -> {
            while (server.isRunning()) {
                try {
                    Thread.sleep(5000);
                    for (SocketClient client : Controller.getInstance().getClients().values()) {
                        Hello hello = new Hello(server.getUserId());
                        try {
                            client.send(hello.createFormat());
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }));
        server.getHelloThread().start();
    }
}
