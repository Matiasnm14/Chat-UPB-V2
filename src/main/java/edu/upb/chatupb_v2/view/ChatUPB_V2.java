/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.network.ChatServer;

public class ChatUPB_V2 {

   public static void main(String[] args) {
        try {
            ChatServer chatServer = new ChatServer();
            chatServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("ChatServer started..");
    }
}
