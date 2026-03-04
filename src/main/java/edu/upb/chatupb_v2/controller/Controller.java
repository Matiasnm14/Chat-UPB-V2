package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.commands.*;
import edu.upb.chatupb_v2.model.entities.enums.StatusMessage;
import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;
import edu.upb.chatupb_v2.model.network.ChatServer;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.view.IChatView;
import edu.upb.chatupb_v2.view.JUi;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import lombok.Getter;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;


public class Controller implements SocketClient.SocketListener{
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    @Getter
    private Map<String, JUi> uis = new HashMap<>();
    private static Controller instance;
    private ChatServer chatServer;

    private  IChatView view;
    private  String username;

    private  String userId;
    private SocketClient socketClient;
    private ServerSocket serverSocket;
    private Thread helloThread;
    private boolean isRunning = true;
    @Getter
    private List<SocketClient> pendingClients = new ArrayList<>();

    private HashMap<String, List<ConfirmRecived>> listOfConfirms = new HashMap<>();
    public static Controller getInstance(){
        if (instance == null) instance = new Controller();
        return instance;
    }
    public  void addClients(SocketClient client){
        clients.putIfAbsent(client.getUID(),client);
    }
    public  void delClients(String idUser){clients.remove(idUser);}

    public void notificarUI(Command command) throws Exception {
        if (command instanceof Invitation){
            SocketClient.SocketListener sl = clients
                    .get(((Invitation) command)
                            .getIdUser())
                    .getListener()
                    .get(((Invitation) command)
                            .getIdUser());
            sl.onInvitationReceived((Invitation) command);
        }
        if (command instanceof Accept) {
            SocketClient.SocketListener sl = clients
                    .get(((Accept) command)
                            .getIdUser())
                    .getListener()
                    .get(((Accept) command)
                            .getIdUser());
            sl.onAcceptReceived((Accept) command);
        }
        if (command instanceof Decline){
            for (SocketClient sc : clients.values()){
                SocketClient.SocketListener sl = sc.getListener().get(sc.getUID());
                sl.onDeclineReceived((Decline) command);
            }
        }
        if (command instanceof Hello){
//            for (SocketClient sc : clients.values()){
//                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    onHelloReceived((Hello) command);
//                }
//            }
        }
        if (command instanceof AcceptHello){

//            SocketClient.SocketListener sl = pendingClients.getFirst().getListener().get(((AcceptHello)command).getIdUser());
            onAcceptHelloReceived((AcceptHello) command);
        }
        if (command instanceof DeclineHello){
            for (SocketClient sc : clients.values()){
                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    sl.onDeclineHelloReceived((DeclineHello) command);
                }
            }
        }

        if (command instanceof Chat) {

            onChatReceived((Chat) command);
        }

        if (command instanceof ConfirmRecived){
//            for (SocketClient sc : clients.values()){
//                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    onConfirmedReceived((ConfirmRecived) command);
//                }
//            }
        }

        if (command instanceof DeleteMessage){
            String id_message = ((DeleteMessage) command).getIdMessage();
            MessageDAO.getInstance().delete(id_message);
        }

        if (command instanceof Buzzing){
//            SocketClient.SocketListener sl = clients.get(((Buzzing) command).getIdUser()).getListener().get(((Buzzing) command).getIdUser());
            onBuzzingReceived((Buzzing) command);
        }


        if (command instanceof Bye){
            for (SocketClient sc : clients.values()){
                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    sl.onByeReceived((Bye) command);
                }
            }
        }
    }
    public void initController(String username, String userId, JUi ui){
        this.userId = userId;
        this.username = username;
        this.view = ui;
        try {
            if (chatServer == null) {
                chatServer = new ChatServer(username, userId);
                chatServer.start();
//                startHelloService();
            }
        } catch (IOException e) {
            view.showMessage("No se pudo iniciar el servidor (¿Puerto 1900 ocupado?): " + e.getMessage());
        }
    }
    public void connect(String ip) {
        new Thread(() -> {
            try {
                socketClient = new SocketClient(ip);
                socketClient.setListener(username, userId, Controller.getInstance());

                socketClient.start();


            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> view.showError("Error de conexión: " + e.getMessage()));
                throw new OperationException("No se logro establecer la conexion");
            }


            Invitation myInvite = new Invitation(userId, username);
            pendingClients.add(socketClient);

            try{
                socketClient.send(myInvite.createFormat());
                SwingUtilities.invokeLater(() -> view.updateStatus("Status: Enviando invitación..."));
            }catch (Exception e){
                SwingUtilities.invokeLater(() -> view.showError("Error de conexión: " + e.getMessage()));
                throw new OperationException("No se logro mandar la invitacion!");
            }

        }).start();
    }

    public void sendMessage(String messageText, String destinationId) {
        if (destinationId == null || destinationId.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> view.showError("Selecciona un contacto primero."));
            return;
        }

        Chat chat = new Chat(this.userId, UUID.randomUUID().toString(), messageText);

        Message msgDb = new Message(
                chat.getIdMessage(),
                destinationId,
                messageText,
                TypeMessage.TEXT,
                StatusMessage.SENT,
                LocalDate.now().toString()
        );


        try {
            MessageDAO.getInstance().save(msgDb);

        } catch (Exception e) {
            throw new OperationException("Error en guardar el Mensaje en la base de datos");
        }

        SocketClient sc = Controller.getInstance().getClients().get(destinationId);

        try{
            if (sc != null) {
                sc.send(chat.createFormat());
                SwingUtilities.invokeLater(() -> view.showMessage("Tú | " + messageText + " |Enviado"));
            } else {
                SwingUtilities.invokeLater(() -> view.showError("El contacto no está en línea en este momento, pero el mensaje se guardó."));
            }
        }catch (Exception e){
            throw new OperationException("Error en enviar el chat a SocketClient");
        }
    }

    public void sendBuzz() {
        for (SocketClient sc : Controller.getInstance().getClients().values()) {
            Buzzing bz = new Buzzing(this.userId);
            try {
                sc.send(bz.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void sendBye(){
        for (SocketClient sc : Controller.getInstance().getClients().values()) {
            Bye bye = new Bye(this.userId);
            try {
                sc.send(bye.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void startHelloService() {
        helloThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(5000);
                    for (SocketClient client : Controller.getInstance().getClients().values()) {
                        Hello hello = new Hello(userId);
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
        });
        helloThread.start();
    }

    public void sendHello(String ip, String username, String id){
        SocketClient sc;
        try{
            sc = new SocketClient(ip);
            sc.setUserName(username);
            sc.setUid(id);
            sc.setListener(username,id,this);
            System.out.println(sc.getListener().get(id));
            pendingClients.add(sc);

            sc.start();


        }catch (Exception e){
            throw new OperationException("No se logro conectar con el cliente");
        }

        Hello hello = new Hello(this.userId);

        try {
            sc.send(hello.createFormat());
        }catch (Exception e){
            throw new OperationException("No se logro enviar el Hello");
        }
    }

    public void markRead(Contact contact){
        try{
            if(contact !=null && clients.containsKey(contact.getId())){
                SocketClient sc = clients.get(contact.getId());
                if(listOfConfirms.containsKey(contact.getId())){
                    for (ConfirmRecived cr : listOfConfirms.get(contact.getId())){
                        sc.send(cr.createFormat());
                    }
                    listOfConfirms.get(contact.getId()).clear();
                    listOfConfirms.remove(contact.getId());
                }
            }
        }catch (Exception e){

        }

    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        boolean accepted = view.showInvitationDialog(invitation.getUserName(), invitation.getIdUser());
        pendingClients.getFirst().setUid(invitation.getIdUser());
        Controller.getInstance().addClients(pendingClients.getFirst());
        pendingClients.removeFirst();

        if (accepted) {
//            Controller.getInstance().addContact(invitation.getUserName());
            Accept acp = new Accept(userId, username);
            try {
                Contact nuevoContacto = new Contact();
                nuevoContacto.setId(invitation.getIdUser());
                nuevoContacto.setName(invitation.getUserName());
                nuevoContacto.setIp(Controller.getInstance().getClients().get(invitation.getIdUser()).getIp());
                nuevoContacto.setUserId(this.userId);

                nuevoContacto.setStateConnect(true);
                System.out.println("Contacto 1:" + nuevoContacto.getId());

                System.out.println("IP: "+nuevoContacto.getIp());;
                if(ContactDao.getInstance().existByCode(nuevoContacto.getId())){
                    System.out.println("Se actualizo la IP");
                    ContactDao.getInstance().updateIp(nuevoContacto.getId(),nuevoContacto.getIp());
                }else {
                    ContactDao.getInstance().save(nuevoContacto);
                    SwingUtilities.invokeLater(() -> {
                        nuevoContacto.setId(invitation.getIdUser());
                        view.onAddModel(nuevoContacto);
                    });
                }


                SocketClient sc = Controller.getInstance().getClients().get(invitation.getIdUser());
                if (sc != null) {
                    sc.send(acp.createFormat());
                }
            } catch (Exception e) {
                System.out.println("Error procesando invitación aceptada: " + e.getMessage());
            }
        } else {
            Decline dec = new Decline();
            try {
                SocketClient sc = Controller.getInstance().getClients().get(invitation.getIdUser());
                if (sc != null)
                    sc.send(dec.createFormat());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onAcceptReceived(Accept accept) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Size: "+pendingClients.size());
            if(!pendingClients.isEmpty()){
                pendingClients.getFirst().setUid(accept.getIdUser());
                pendingClients.getFirst().setUserName(accept.getUserName());
                Controller.getInstance().addClients(pendingClients.getFirst());
                pendingClients.removeFirst();
            }
            view.updateStatus("Status: Online");
            view.showMessage("Conexión Aceptada");
            try {
                SocketClient sc = Controller.getInstance().getClients().get(accept.getIdUser());
                if (sc != null) {
                    Contact nuevoContacto = new Contact();
                    nuevoContacto.setId(accept.getIdUser());
                    nuevoContacto.setName(accept.getUserName());
                    nuevoContacto.setIp(sc.getIp());
                    nuevoContacto.setUserId(this.userId);
                    nuevoContacto.setStateConnect(true);


                    if(ContactDao.getInstance().existByCode(nuevoContacto.getId())){
                        System.out.println("Se actualizo la IP");
                        ContactDao.getInstance().updateIp(nuevoContacto.getId(),nuevoContacto.getIp());
                    }else{
                        ContactDao.getInstance().save(nuevoContacto);
//                    nuevoContacto.setId(accept.getIdUser());
                        view.onAddModel(nuevoContacto);
                    }

                    view.updateStatus("Status: Online");
                    view.showMessage("Conexión Aceptada con " + accept.getUserName());
                }
            } catch (Exception e) {
                System.out.println("Error guardando contacto al aceptar: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDeclineReceived(Decline decline) {
        SwingUtilities.invokeLater(() -> {
            view.updateStatus("Status: Rejected");
            view.showMessage("Conexión Rechazada");
            Controller.getInstance().delClients(socketClient.getUID());
            if(socketClient != null) socketClient.close();
        });
    }

    @Override
    public void onHelloReceived(Hello hello) {
        String senderId = hello.getIdUser();

        SocketClient incomingSocket = null;
        for (SocketClient sc : pendingClients) {
            if (sc.getUID() == null || !sc.getUID().equals(senderId)) {
                incomingSocket = sc;
                break;
            }
        }

        if (incomingSocket == null && !pendingClients.isEmpty()) {
            incomingSocket = pendingClients.getFirst();
        }
        if (incomingSocket == null) return;

        incomingSocket.setUid(senderId);


        try {
            if (ContactDao.getInstance().existByCode(senderId)) {
                incomingSocket.setName(ContactDao.getInstance().findById(hello.getIdUser()).getName());
                System.out.println("onHelloReceived: Contacto reconocido!");

//                ContactDao.getInstance().updateStatus(senderId);
                view.onLoadContacts(ContactDao.getInstance().findAll());

                pendingClients.remove(incomingSocket);
                addClients(incomingSocket);

                AcceptHello accept = new AcceptHello(this.userId);
                incomingSocket.send(accept.createFormat());
            } else {
                pendingClients.remove(incomingSocket);
                DeclineHello declineHello = new DeclineHello();
                incomingSocket.send(declineHello.createFormat());
                incomingSocket.close();
            }
        } catch (Exception e) {
            System.out.println("Error en onHelloReceived: " + e.getMessage());
        }

    }

    @Override
    public void onAcceptHelloReceived(AcceptHello acceptHello) {
        String responderId = acceptHello.getIdUser();

        try {
//            ContactDao.getInstance().updateStatus(responderId);
            view.onLoadContacts(ContactDao.getInstance().findAll());

            SocketClient outgoingSocket = null;
            for (SocketClient sc : pendingClients) {
                if (responderId.equals(sc.getUID())) {
                    outgoingSocket = sc;
                    break;
                }
            }

            if (outgoingSocket != null) {
                pendingClients.remove(outgoingSocket);
                addClients(outgoingSocket);
                System.out.println("Conexión reestablecida con éxito.");
            }

        } catch (Exception e) {
            System.out.println("Error en onAcceptHelloReceived: " + e.getMessage());
        }
    }

    @Override
    public void onDeclineHelloReceived(DeclineHello declineHello) {
        try{

            for(SocketClient sc : pendingClients){
                if(sc.getUID().equals(this.userId))continue;
                socketClient.close();
                pendingClients.removeFirst();
                break;
            }
        }catch (Exception e){}
    }

    @Override
    public void onChatReceived(Chat chat) {
        if(view.getCurrentContact() !=null && view.getCurrentContact().getId().equals(chat.getIdUser()))
            view.showChat(chat);
        try {
            Message msgDb = new Message(
                    chat.getIdMessage(),
                    chat.getIdUser(),
                    chat.getMessage(),
                    TypeMessage.TEXT,
                    StatusMessage.RECEIVED,
                    LocalDate.now().toString()
            );
            MessageDAO.getInstance().save(msgDb);

        } catch (Exception e) {
            System.out.println("Error al guardar mensaje recibido: " + e.getMessage());
        }
        ConfirmRecived confirmRecived = new ConfirmRecived(chat.getIdMessage());
        SocketClient client = Controller.getInstance().getClients().get(chat.getIdUser());
        try {
            if(view.getCurrentContact() != null && view.getCurrentContact().getId().equals(chat.getIdUser())){
                client.send(confirmRecived.createFormat());
            }else{
                if(!listOfConfirms.containsKey(chat.getIdUser())){
                    listOfConfirms.put(chat.getIdUser(),new ArrayList<>());
                }
                listOfConfirms.get(chat.getIdUser()).add(confirmRecived);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onConfirmedReceived(ConfirmRecived confirmRecived) {
        try{
            if(MessageDAO.getInstance().existById(confirmRecived.getIdMessage())){
                System.out.println("Se recibió confirmación del mensaje: " + confirmRecived.getIdMessage());

                MessageDAO.getInstance().updateStatus(confirmRecived.getIdMessage());

                SwingUtilities.invokeLater(() -> {
                    view.refreshChatView();

                });

            }

        }catch (Exception e){

        }

    }

    @Override
    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {

    }

    @Override
    public void onBuzzingReceived(Buzzing buzzing) {
        String name = "Desconocido";
        SocketClient sc = Controller.getInstance().getClients().get(buzzing.getIdUser());
        if (sc != null) {
            name = sc.getNombre();
        }
        String finalName = name;
        SwingUtilities.invokeLater(() -> view.showBuzzNotification(finalName));
    }

    @Override
    public void onPinMessageReceived(PinMessage pinMessage) {

    }

    @Override
    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {

    }

    @Override
    public void onThemeReceived(Theme theme) {

    }

    @Override
    public void onByeReceived(Bye bye) {
        String id = bye.getIdUser();
        System.out.println("ID: " + id );
        SocketClient sc = Controller.getInstance().getClients().get(id);
        if (sc != null){
            sc.close();
        }
        SwingUtilities.invokeLater(() -> view.showByeNotification(id));
    }
}
