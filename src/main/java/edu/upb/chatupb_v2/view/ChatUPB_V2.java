/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.ChatServer;
import edu.upb.chatupb_v2.model.repository.ContactDao;

public class ChatUPB_V2 {

   public static void main(String[] args) {
        ContactDao contactDao = new ContactDao();
        try {
            ChatServer chatServer = new ChatServer();
            chatServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("ChatServer started..");
    }
/*
        try {
            SocketClient socketClient = new SocketClient("localhost");
            socketClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
