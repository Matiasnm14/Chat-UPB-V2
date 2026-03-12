package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.network.ChatServer;
import edu.upb.chatupb_v2.model.repository.ContactDao;
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

    private Map<String, List<ConfirmRecived>> confirms = new HashMap<>();


    public static Controller getInstance() {
        if (instance == null) instance = new Controller();
        return instance;
    }

    public void addClients(SocketClient client) {
        clients.putIfAbsent(client.getUID(), client);
    }

    public void delClients(String idUser) {
        clients.remove(idUser);
        uis.get(server.getUserId()).updateContacts();
    }

    @Override
    public void notificarUI(Command command, String clientId) throws Exception {
        for (JUi sl : uis.values()) {
            if (command instanceof Invitation) {
//                SocketClient.SocketListener sl = clients.get(((Invitation) command).getIdUser()).getListener().get(((Invitation) command).getIdUser());
                boolean accecpted = sl.onInvitationReceived((Invitation) command);
                Invitation invitation = (Invitation) command;
                SocketClient sc = Controller.getInstance().getClients().get(invitation.getIdUser());
                if (accecpted) {
                    if (sc != null) {
                        Accept acp = new Accept(server.getUserId(), server.getUsername());
                        sc.send(acp.createFormat());
                    }
                    if (ContactDao.getInstance().existByCode(invitation.getIdUser())){
                        ContactDao.getInstance().updateContact(invitation.getIdUser(), sc.getIp());
                    }
                }else {
                    Decline dec = new Decline();
                    try {
                        sc = Controller.getInstance().getClients().get(invitation.getIdUser());
                        if (sc != null)
                            sc.send(dec.createFormat());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (command instanceof Accept) {
                Accept accept = (Accept) command;
                SwingUtilities.invokeLater(() -> {

                    Controller.getInstance().getPendingClients().getFirst().setUid(accept.getIdUser());
                    Controller.getInstance().getPendingClients().getFirst().setUserName(accept.getUserName());
                    Controller.getInstance().addClients(Controller.getInstance().getPendingClients().getFirst());
                    Controller.getInstance().getPendingClients().removeFirst();

//            Controller.getInstance().addContact(accept.getUserName());

                    try {
                        SocketClient sc = Controller.getInstance().getClients().get(accept.getIdUser());
                        if (sc != null) {
                            if (!ContactDao.getInstance().existByCode(accept.getIdUser())) {
                                Contact contact = sl.onAcceptReceived(accept, sc.getIp());
                                ContactDao.getInstance().save(contact);
                                contact.setId(accept.getIdUser());
                            }
                        }
                        if (ContactDao.getInstance().existByCode(accept.getIdUser())){
                            ContactDao.getInstance().updateContact(accept.getIdUser(), sc.getIp());
                        }
                        uis.get(server.getUserId()).updateContacts();
                    } catch (Exception e) {
                        System.out.println("Error guardando contacto al aceptar: " + e.getMessage());
                    }
                });

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
                Hello hello = (Hello) command;
                if (!pendingClients.isEmpty()) {
                    SocketClient client = pendingClients.getFirst();
                    client.setUid(hello.getIdUser());
                    if (client != null) {
                        try {
                            if (ContactDao.getInstance().existByCode(hello.getIdUser())) {
                                pendingClients.remove(client);
                                client.setUserName(ContactDao.getInstance().findById(hello.getIdUser()).getName());
                                addClients(client);
                                ContactDao.getInstance().updateContact(hello.getIdUser(), client.getIp());
                                AcceptHello acceptHello = new AcceptHello(server.getUserId());
                                client.send(acceptHello.createFormat());
                                uis.get(server.getUserId()).updateContacts();
                            } else {
                                pendingClients.remove(client);
                                DeclineHello declineHello = new DeclineHello();
                                client.send(declineHello.createFormat());
                                client.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new OperationException("No se pudo enviar respuesta a Hello");
                        }
                    }
                }
            }
            if (command instanceof AcceptHello) {
                AcceptHello acceptHello = (AcceptHello) command;
                sl.onAcceptHelloReceived(acceptHello);
                SocketClient socketClient = pendingClients.getFirst();
                socketClient.setUid(acceptHello.getIdUser());
                    if (socketClient.getUID().equals(acceptHello.getIdUser())){
                        addClients(socketClient);
                        uis.get(server.getUserId()).updateContacts();
                        break;
                    }
            }
            if (command instanceof DeclineHello) {

                for (SocketClient socketClient : pendingClients){
                    if (socketClient.getUID() == server.getUserId()) continue;
                    socketClient.close();
                    pendingClients.remove(socketClient);
                    break;
                }

                sl.onDeclineHelloReceived((DeclineHello) command);


            }

            if (command instanceof Chat) {
                Chat chat = (Chat) command;
                ConfirmRecived confirmRecived = sl.onChatReceived(chat);

                SocketClient client = Controller.getInstance().getClients().get(chat.getIdUser());
                try {
                    if (sl.getCurrentContact() != null && sl.getCurrentContact().getId().equals(client.getUID())) {
                        client.send(confirmRecived.createFormat());
                        System.out.println("Confirmacion enviada a: " + client.getNombre());
                    }else {
                        if (confirms.containsKey(client.getUID())){
                            confirms.get(client.getUID()).add(confirmRecived);
                        }else {
                            confirms.put(client.getUID(), new ArrayList<>());
                            confirms.get(client.getUID()).add(confirmRecived);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            if (command instanceof ConfirmRecived) {


                    sl.onConfirmedReceived((ConfirmRecived) command);


            }

            if (command instanceof DeleteMessage) {
                String id_message = ((DeleteMessage) command).getIdMessage();
                MessageDAO.getInstance().delete(id_message);
            }

            if (command instanceof Buzzing) {
                Buzzing buzzing = (Buzzing) command;
                String name = "Desconocido";
                SocketClient sc = Controller.getInstance().getClients().get(buzzing.getIdUser());
                if (sc != null) {
                    name = sc.getNombre();
                }
                String finalName = name;

                sl.onBuzzingReceived(finalName);
            }


            if (command instanceof Bye) {

                Bye bye = (Bye) command;
                String id = bye.getIdUser();
                System.out.println("ID: " + id );
                SocketClient sc = Controller.getInstance().getClients().get(id);
                if (sc != null){
                    sc.close();
                }
                sl.onByeReceived();



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
//                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(server.getUserId()).
//                        showMessage("Tú | " + messageText));
            } else {
                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(server.getUserId()).
                        showError("El contacto no está en línea en este momento, pero el mensaje se guardó."));
            }

        } catch (Exception e) {
            throw new OperationException("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    public void sendHello(String ip, String username, String id){
        SocketClient sc;
        try {
            sc = new SocketClient(ip);
            sc.setListener(username, id, this);
            pendingClients.add(sc);

            sc.start();




        }catch (Exception e){
            throw new OperationException("No se pudo conectar con el contacto");
        }

        Hello hello = new Hello(server.getUserId());
        try {
            sc.send(hello.createFormat());

        }catch (Exception e){
            throw new OperationException("No se pudo enviar Hello");
        }
    }

    public void sendBuzz(String clientId) {
        SocketClient sc = clients.get(clientId);
        if (sc != null) {
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

    public void sendConfirms(String id){
        SocketClient client = clients.get(id);
        if (client != null){
            if (confirms.containsKey(client.getUID())){
                for (int i = 0; i < confirms.get(client.getUID()).size(); i++) {

                    ConfirmRecived confirmRecived = confirms.get(client.getUID()).get(i);
                    try {
                        client.send(confirmRecived.createFormat());


                    }catch (Exception e){
                        throw new OperationException("Error al enviar confirmacion");
                    }
                }
                confirms.get(client.getUID()).clear();
                if (confirms.get(client.getUID()).isEmpty())
                    confirms.remove(client.getUID());
            }
        }
    }
}
