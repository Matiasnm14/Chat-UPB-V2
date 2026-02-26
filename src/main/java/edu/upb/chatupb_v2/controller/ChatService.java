package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.commands.*;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.view.IChatView;
import edu.upb.chatupb_v2.view.JUi;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import edu.upb.chatupb_v2.model.entities.enums.StatusMessage;
import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatService implements SocketClient.SocketListener{
    private final IChatView view;
    private final String username;
    private final String userId;
    private SocketClient socketClient;
    private ServerSocket serverSocket;
    private Thread helloThread;
    private boolean isRunning = true;
    private List<SocketClient> pendingClients = new ArrayList<>();

    public ChatService(IChatView view, String username, String userId) {
        this.view = view;
        this.username = username;
        this.userId = userId;
        startServer();
        startHelloService();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(1900);
                System.out.println("Servidor escuchando en puerto 1900...");

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    SocketClient newClient = new SocketClient(clientSocket);

                    newClient.setListener(username, userId, this);
                    pendingClients.add(newClient);
                    newClient.start();
                    System.out.println("Nuevo cliente conectado desde: " + clientSocket.getInetAddress());
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        view.showError("No se pudo iniciar el servidor (¿Puerto 1900 ocupado?): " + e.getMessage())
                );
            }
        }).start();
    }
    public void connect(String ip) {
        new Thread(() -> {
            try {
                socketClient = new SocketClient(ip);
                socketClient.setListener(username, userId, this);

                socketClient.start();

                Invitation myInvite = new Invitation(userId, username);
                pendingClients.add(socketClient);
                socketClient.send(myInvite.createFormat());



                SwingUtilities.invokeLater(() -> view.updateStatus("Status: Enviando invitación..."));

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> view.showError("Error de conexión: " + e.getMessage()));
            }
        }).start();
    }

    public void sendMessage(String messageText, String destinationId) {
        if (destinationId == null || destinationId.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> view.showError("Selecciona un contacto primero."));
            return;
        }

        try {
            Chat chat = new Chat(this.userId, UUID.randomUUID().toString(), messageText);

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
                SwingUtilities.invokeLater(() -> view.showMessage("Tú | " + messageText));
            } else {
                SwingUtilities.invokeLater(() -> view.showError("El contacto no está en línea en este momento, pero el mensaje se guardó."));
            }

        } catch (Exception e) {
            System.out.println("Error al enviar mensaje: " + e.getMessage());
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
                ContactDao.getInstance().save(nuevoContacto);

                SwingUtilities.invokeLater(() -> {
                    if (view instanceof JUi) {
                        System.out.println("Contacto 2:" + nuevoContacto.getId());
                        nuevoContacto.setId(invitation.getIdUser());
                        ((JUi) view).addModel(nuevoContacto);
                    }
                });

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
            if(pendingClients.size() != 0){
                pendingClients.getFirst().setUid(accept.getIdUser());
                pendingClients.getFirst().setUserName(accept.getUserName());
                Controller.getInstance().addClients(pendingClients.getFirst());
                pendingClients.removeFirst();
            }

//            Controller.getInstance().addContact(accept.getUserName());
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
                    System.out.println("Nuevo Contacto: "+nuevoContacto.getId());
                    ContactDao.getInstance().save(nuevoContacto);

                    if (view instanceof JUi) {
                        nuevoContacto.setId(accept.getIdUser());
                        System.out.println("Nuevo Contacto 2:"+nuevoContacto.getId());
                        ((JUi) view).addModel(nuevoContacto);
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
        AcceptHello acceptHello = new AcceptHello(userId);
        SocketClient client = Controller.getInstance().getClients().get(hello.getIdUser());
        if (client != null) {
            try {
                client.send(acceptHello.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void onChatReceived(Chat chat) {
        view.showChat(chat);
        try {
            Message msgDb = new Message(
                    chat.getIdMessage(),
                    chat.getIdUser(),
                    chat.getMessage(),
                    TypeMessage.TEXT,
                    StatusMessage.READ,
                    LocalDate.now().toString()
            );
            MessageDAO.getInstance().save(msgDb);

        } catch (Exception e) {
            System.out.println("Error al guardar mensaje recibido: " + e.getMessage());
        }
        ConfirmRecived confirmRecived = new ConfirmRecived(chat.getIdMessage());
        SocketClient client = Controller.getInstance().getClients().get(chat.getIdUser());
        try {
            client.send(confirmRecived.createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    @Override public void onByeReceived(Bye bye){
        String id = bye.getIdUser();
        System.out.println("ID: " + id );
        SocketClient sc = Controller.getInstance().getClients().get(id);
        if (sc != null){
            sc.close();
        }
        SwingUtilities.invokeLater(() -> view.showByeNotification(id));
    }
    @Override public void onAcceptHelloReceived(AcceptHello acceptHello) {}
    @Override public void onDeclineHelloReceived(DeclineHello declineHello) {}
    @Override public void onConfirmedReceived(ConfirmRecived confirmRecived) {
        System.out.println("Recibido");
//        try {
//            MessageDAO.getInstance().updateMessage(confirmRecived.getIdMessage());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
    @Override
    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {
//        String id_message = deleteMessage.getIdMessage();
//        try {
//            MessageDAO.getInstance().delete(id_message);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
    @Override public void onPinMessageReceived(PinMessage pinMessage) {}
    @Override public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {
        System.out.println("MENSAJE ÚNICO");
    }
    @Override public void onThemeReceived(Theme theme) {}
}
