package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.repository.MessageDAO;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.ArrayList;
import java.util.List;

public class MessageController {
    private final MessageDAO messageDao;
    private final IChatView iChatView;

    public MessageController(IChatView iChatView) {
        this.messageDao = new MessageDAO();
        this.iChatView = iChatView;
    }

    public void unloadForContact(String userId, String contactCode, String roomCode) {
        if (iChatView == null) {
            return;
        }
        try {
            List<MessageDAO.Message> messages = new ArrayList<>();
            if (contactCode != null && !contactCode.isBlank()) {
                messages = messageDao.findByParticipants(userId, contactCode);
            }
            if (messages.isEmpty() && roomCode != null && !roomCode.isBlank()) {
                messages = messageDao.findByRoomCode(roomCode);
            }
            iChatView.unloadMessages(messages);
        } catch (Exception e) {
            iChatView.showError("No se pudieron cargar los mensajes: " + e.getMessage());
        }
    }
}
// nO USAR FINDALL, BUSCAMOS LOS MENSAGES QUE YO TENGO CON ESE CONTACTO, LA BASE DE DATOS ES LA QUE ESTQ FILTRANDO PAGINACION
//cambiar a un id
