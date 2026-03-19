package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.commands.*;
import edu.upb.chatupb_v2.model.entities.enums.Sound;
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
import java.net.ConnectException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;


public class Controller implements SocketClient.SocketListener{
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    private static Controller instance;
    private ChatServer chatServer;

    private  IChatView view;
    private  String username;
    @Getter
    private  String userId;
    private SocketClient socketClient;
    private ServerSocket serverSocket;
    @Getter
    private List<SocketClient> pendingClients = new ArrayList<>();
    private final long timerBuzz = 3000;
    private long lastBuzz = 0;
    private HashMap<String, List<ConfirmRecived>> listOfConfirms = new HashMap<>();
    public static Controller getInstance(){
        if (instance == null) instance = new Controller();
        return instance;
    }
    public  void addClients(SocketClient client){
        clients.putIfAbsent(client.getUID(),client);
    }
    public  void delClients(String idUser){
        clients.remove(idUser);
        try{
            view.onLoadContacts(ContactDao.getInstance().findAll());
        }catch (Exception e){

        }

    }

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
        if (command instanceof ImageMesagge){
            onImageReceived((ImageMesagge) command);
        }
        if(command instanceof DeleteMessage){
            onDeleteMessageReceived((DeleteMessage) command);
        }
        if(command instanceof Theme){
            onThemeReceived((Theme) command);
        }
        if(command instanceof UniqueMessage){
            onUniqueMessageReceived((UniqueMessage) command);
        }
        if(command instanceof PinMessage){
            onPinMessageReceived((PinMessage) command);
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

    public void sendMessage(Command chat, String destinationId) {
        if (destinationId == null || destinationId.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> view.showError("Selecciona un contacto primero."));
            return;
        }
        SocketClient sc = Controller.getInstance().getClients().get(destinationId);
        chat.executed(sc);
    }
    public void sendImage(Command chat, String destinationId){
        if (destinationId == null || destinationId.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> view.showError("Selecciona un contacto primero."));
            return;
        }
        SocketClient sc = Controller.getInstance().getClients().get(destinationId);
        chat.executed(sc);

    }

    public void sendBuzz(String idDestination) {
            Buzzing bz = new Buzzing(this.userId);
            try {
                SocketClient sc = Controller.getInstance().getClients().get(idDestination);
                sc.send(bz.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
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
    public void deleteMessage(Message message){
        try {
            Contact contact = ContactDao.getInstance().findById(message.getContactId());
            if (contact != null && contact.getIdPinMessage() != null && contact.getIdPinMessage().equals(message.getIdMessage())) {

                ContactDao.getInstance().setIdPin(message.getContactId(), null);

                if (view.getCurrentContact() != null && view.getCurrentContact().getId().equals(message.getContactId())) {
                    view.getCurrentContact().setIdPinMessage(null);
                    view.updatePinnedMessageUI(null);
                }
            }


            MessageDAO.getInstance().delete(message.getIdMessage());
            view.refreshChatView();
        }catch (Exception e){

        }
    }
    public void sendDeleteMessage(Message message, String idCurrentContact){
        DeleteMessage deleteMessage = new DeleteMessage(message.getIdMessage());
        SocketClient sc = clients.get(idCurrentContact);
        try{

            Contact contact = ContactDao.getInstance().findById(message.getContactId());
            if (contact != null && contact.getIdPinMessage() != null && contact.getIdPinMessage().equals(message.getIdMessage())) {

                ContactDao.getInstance().setIdPin(message.getContactId(), null);

                if (view.getCurrentContact() != null && view.getCurrentContact().getId().equals(message.getContactId())) {
                    view.getCurrentContact().setIdPinMessage(null);
                    view.updatePinnedMessageUI(null);
                }
            }

            sc.send(deleteMessage.createFormat());
            MessageDAO.getInstance().delete(message.getIdMessage());
            view.refreshChatView();

        }catch (Exception e){

        }
    }
    public void sendTheme(String idCurrentContact, String idTheme){
        if (clients.containsKey(idCurrentContact)) {
            Theme themeCmd = new Theme(this.userId, idTheme);
            try {
                ContactDao.getInstance().setIdTheme(idCurrentContact,idTheme);
                clients.get(idCurrentContact).send(themeCmd.createFormat());


                if (view.getCurrentContact() != null) {
                    view.getCurrentContact().setIdTheme(idTheme);
                }

                view.applyTheme(idTheme);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("Comando de tema enviado: " + themeCmd.createFormat());
        }
    }

    public void sendUniqueMessage(String texto, String targetId) {
        if (clients.containsKey(targetId)) {
            // Creamos el comando
            UniqueMessage uniqueCmd = new UniqueMessage(this.userId, UUID.randomUUID().toString(), texto);

            // Lo enviamos por Sockets
            try {
                clients.get(targetId).send(uniqueCmd.createFormat());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Opcional: Mostrarlo en mi propia pantalla como un mensaje normal o como UNIQUE
            Message msgLocal = new Message(uniqueCmd.getIdMessage(), this.userId, texto, TypeMessage.UNIQUE, StatusMessage.SENT, LocalDate.now().toString());
            view.appendMessageToChat(msgLocal);
        }
    }

    public void sendPinMessage(Message msg, String idCurrentContact) {
        if (clients.containsKey(idCurrentContact)) {
            PinMessage pinCmd = new PinMessage(msg.getIdMessage());
            try {
                // 1. Mandamos la trama
                clients.get(idCurrentContact).send(pinCmd.createFormat());

                // 2. Guardamos en la Base de Datos
                ContactDao.getInstance().setIdPin(idCurrentContact, msg.getIdMessage());

                // 3. Actualizamos la memoria RAM (¡Muy importante!)
                if (view.getCurrentContact() != null) {
                    view.getCurrentContact().setIdPinMessage(msg.getIdMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void sendUniqueMessageSeen(Message message, String idContact){
        try{
            SocketClient sc = clients.get(idContact);

            ConfirmRecived confirmRecived = new ConfirmRecived(message.getIdMessage());

            sc.send(confirmRecived.createFormat());

        }catch (Exception e){}

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
                    ContactDao.getInstance().findById(nuevoContacto.getId()).setStateConnect(true);
                }else {
                    ContactDao.getInstance().save(nuevoContacto);
                    SwingUtilities.invokeLater(() -> {
                        nuevoContacto.setId(invitation.getIdUser());
                        view.onAddModel(nuevoContacto);
                    });
                }
                view.onLoadContacts(ContactDao.getInstance().findAll());


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
                addClients(pendingClients.getFirst());
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
                        ContactDao.getInstance().findById(nuevoContacto.getId()).setStateConnect(true);
                    }else{
                        ContactDao.getInstance().save(nuevoContacto);
                        nuevoContacto.setId(accept.getIdUser());
                        view.onAddModel(nuevoContacto);
                    }

                    view.onLoadContacts(ContactDao.getInstance().findAll());

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
            incomingSocket.setUserName(ContactDao.getInstance().findById(senderId).getName());
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        try {
            if (ContactDao.getInstance().existByCode(senderId)) {
                incomingSocket.setName(ContactDao.getInstance().findById(hello.getIdUser()).getName());
                System.out.println("onHelloReceived: Contacto reconocido!");

                ContactDao.getInstance().findById(hello.getIdUser()).setStateConnect(true);
                ContactDao.getInstance().updateIp(hello.getIdUser(),incomingSocket.getIp());

                System.out.println("IP ACTUALIZADA EN HELLO: "+ incomingSocket.getIp());


                pendingClients.remove(incomingSocket);
                addClients(incomingSocket);
                view.onLoadContacts(ContactDao.getInstance().findAll());

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
            view.onLoadContacts(ContactDao.getInstance().findAll());
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

            SwingUtilities.invokeLater(() -> {
                view.showChat(chat);
            });

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
        Sound.NEW_MESSAGE.play();
//

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
        try{
            Contact contact = ContactDao.getInstance().findById(MessageDAO.getInstance().findById(deleteMessage.getIdMessage()).getContactId());
            if (contact != null && contact.getIdPinMessage() != null && contact.getIdPinMessage().equals(deleteMessage.getIdMessage())) {

                ContactDao.getInstance().setIdPin(contact.getId(), null);

                if (view.getCurrentContact() != null && view.getCurrentContact().getId().equals(contact.getId())) {
                    view.getCurrentContact().setIdPinMessage(null);
                    view.updatePinnedMessageUI(null);
                }
            }
            MessageDAO.getInstance().delete(deleteMessage.getIdMessage());
            view.refreshChatView();
            System.out.println("CHAT REFRESH");
        }catch (Exception e){

        }
    }

    @Override
    public void onBuzzingReceived(Buzzing buzzing) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastBuzz > timerBuzz){
            lastBuzz = System.currentTimeMillis();
            String name = "Desconocido";

            try {
                name = ContactDao.getInstance().findById(buzzing.getIdUser()).getName();
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            String finalName = name;
            SwingUtilities.invokeLater(() -> view.showBuzzNotification(finalName));
        }
    }

    @Override
    public void onPinMessageReceived(PinMessage pinMessage) {
        try {
            String senderId = MessageDAO.getInstance().findById(pinMessage.getIdMessage()).getContactId();
            ContactDao.getInstance().setIdPin(senderId, pinMessage.getIdMessage());
            if (view.getCurrentContact() != null && view.getCurrentContact().getId().equals(senderId)) {
                Message msg = MessageDAO.getInstance().findById(pinMessage.getIdMessage());
                if (msg != null) {
                    SwingUtilities.invokeLater(() -> {
                        view.updatePinnedMessageUI(msg.getBody());
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {
        Message msgEnMemoria = new Message(
                uniqueMessage.getIdMessage(),
                uniqueMessage.getIdUser(),
                uniqueMessage.getMessage(),
                TypeMessage.UNIQUE,
                StatusMessage.RECEIVED,
                LocalDate.now().toString()
        );

        SwingUtilities.invokeLater(() -> {
            view.appendMessageToChat(msgEnMemoria);
        });
    }

    @Override
    public void onThemeReceived(Theme theme) {
        try {
            ContactDao.getInstance().setIdTheme(theme.getIdUser(),theme.getIdTheme());
            if (view.getCurrentContact() != null) {
                view.getCurrentContact().setIdTheme(theme.getIdTheme());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        view.applyTheme(theme.getIdTheme());
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

    @Override
    public void onImageReceived(ImageMesagge imageMesagge) {
        try {
            // 1. Le agregamos el prefijo para que el Render sepa que esto es una imagen
            String payload = imageMesagge.getImage().toString();

            // 2. Guardamos en la base de datos como un mensaje normal
            Message msgDb = new Message(
                    imageMesagge.getIdMessage(),
                    imageMesagge.getIdUser(),
                    payload,
                    TypeMessage.IMAGE, // O TypeMessage.IMAGE si lo agregaste a tu Enum
                    StatusMessage.RECEIVED,
                    LocalDate.now().toString()
            );
            MessageDAO.getInstance().save(msgDb);

            // 3. Avisamos a la UI que muestre la imagen (en el hilo de Swing)
            SwingUtilities.invokeLater(() -> {
                view.showImageMessage(imageMesagge);
            });

        } catch (Exception e) {
            System.out.println("Error al guardar la imagen recibida: " + e.getMessage());
        }

        // 4. Enviar confirmación de recibido al otro cliente
        ConfirmRecived confirmRecived = new ConfirmRecived(imageMesagge.getIdMessage());
        SocketClient client = Controller.getInstance().getClients().get(imageMesagge.getIdUser());

        try {
            if(view.getCurrentContact() != null && view.getCurrentContact().getId().equals(imageMesagge.getIdUser())){
                client.send(confirmRecived.createFormat());
            } else {
                if(!listOfConfirms.containsKey(imageMesagge.getIdUser())){
                    listOfConfirms.put(imageMesagge.getIdUser(), new ArrayList<>());
                }
                listOfConfirms.get(imageMesagge.getIdUser()).add(confirmRecived);
            }
        } catch (Exception e) {
            System.out.println("Error al confirmar recepción de imagen: " + e.getMessage());
        }
    }
}
