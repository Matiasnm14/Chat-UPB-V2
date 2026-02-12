/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.bl.fm.Aceptar;
import edu.upb.chatupb_v2.bl.fm.Invitacion;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;

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

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                String[] incoming_message = message.split("\\|");
                System.out.print("Mensaje de " + incoming_message[0] + ": ");
                System.out.println(incoming_message[1]);
            }
            String[] split = message.split(Pattern.quote("|"));
            if (split.length == 0) {
                return;
            }
            switch (split[0]) {
                case "001": {
                    Invitacion inv = Invitacion.parse(message);
                    System.out.println(inv.generarTrama());
                }
                case "002":
                    Aceptar acp = Aceptar.parse(message);
                    System.out.println(acp.generarTrama());
                default:
                    throw new IllegalStateException("Unexpected value: " + split[0]);
            }
            //send("Hola Server!!!. "+System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
