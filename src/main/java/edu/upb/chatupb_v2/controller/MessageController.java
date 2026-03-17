package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.repository.MessageDAO;
import edu.upb.chatupb_v2.view.ChatMessageViewModel;
import edu.upb.chatupb_v2.view.IChatView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final MessageDAO messageDao;
    private final IChatView iChatView;

    public MessageController(IChatView iChatView) {
        this.messageDao = new MessageDAO();
        this.iChatView = iChatView;
    }

    public void unloadForContact(String userId, String userName, String contactCode, String roomCode) {
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
            iChatView.unloadMessages(toViewModels(messages, userId, userName));
        } catch (Exception e) {
            iChatView.showError("No se pudieron cargar los mensajes: " + e.getMessage());
        }
    }

    private List<ChatMessageViewModel> toViewModels(List<MessageDAO.Message> messages, String userId, String userName) {
        List<ChatMessageViewModel> viewModels = new ArrayList<>();
        if (messages == null) {
            return viewModels;
        }
        for (MessageDAO.Message message : messages) {
            if (message == null) {
                continue;
            }
            boolean outgoing = userId != null && userId.equals(message.getSenderCode());
            viewModels.add(new ChatMessageViewModel(
                    message.getCodMessage(),
                    message.getMessage(),
                    outgoing,
                    outgoing ? userName : "Desconocido",
                    extractTime(message.getCreatedDate()),
                    outgoing && message.getStatusMessage() == edu.upb.chatupb_v2.model.entities.enums.StatusMessage.READ,
                    message.getType(),
                    message.isPinned()
            ));
        }
        return viewModels;
    }

    private String extractTime(String dateValue) {
        if (dateValue == null || dateValue.isBlank()) {
            return LocalDateTime.now().toLocalTime().format(TIME_FORMAT);
        }
        try {
            return LocalDateTime.parse(dateValue, DATE_FORMAT).toLocalTime().format(TIME_FORMAT);
        } catch (Exception e) {
            return LocalDateTime.now().toLocalTime().format(TIME_FORMAT);
        }
    }
}
// nO USAR FINDALL, BUSCAMOS LOS MENSAGES QUE YO TENGO CON ESE CONTACTO, LA BASE DE DATOS ES LA QUE ESTQ FILTRANDO PAGINACION
//cambiar a un id
