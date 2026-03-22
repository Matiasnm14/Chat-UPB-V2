/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.model.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author rlaredo
 */
public class ConnectionDB {

    private static final ConnectionDB connection = new ConnectionDB();
    private static boolean connectionLogged = false;
    private static final String URL = getDatabaseURL();

    private ConnectionDB() {
    }

    public static ConnectionDB getInstance() {
        return connection;
    }
    private static String getDatabaseURL() {
        String appDataPath = System.getenv("APPDATA");
        File folder = new File(appDataPath + File.separator + "ChatUPB");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String dbPath = folder.getAbsolutePath() + File.separator + "chat_upb_v2.sqlite";
        return "jdbc:sqlite:" + dbPath;
    }

    public Connection getConection() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);
            if (conn != null && !connectionLogged) {
                connectionLogged = true;
                System.out.println("Conexion exitosa");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
        }
        return conn;
    }
    public static void initDataBase(){
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stm = conn.createStatement()){

            stm.execute("CREATE TABLE IF NOT EXISTS user_profile (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id TEXT UNIQUE NOT NULL," +
                    "user_name TEXT NOT NULL," +
                    "created_at TEXT," +
                    "theme_id TEXT DEFAULT 'default'" +
                    ")");


            stm.execute("CREATE TABLE IF NOT EXISTS contact (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "code TEXT UNIQUE NOT NULL," +
                    "name TEXT," +
                    "ip TEXT," +
                    "theme_id TEXT DEFAULT 'default'" +
                    ")");

            stm.execute("CREATE TABLE IF NOT EXISTS message (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cod_message TEXT," +
                    "recipient_code TEXT," +
                    "created_date TEXT," +
                    "sender_code TEXT," +
                    "message TEXT," +
                    "type TEXT," +
                    "room_code TEXT," +
                    "status_message TEXT DEFAULT 'SENT'," +
                    "pinned INTEGER DEFAULT 0" +
                    ")");


        }catch (Exception e){

        }
    }
}
