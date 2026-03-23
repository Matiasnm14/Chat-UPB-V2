package edu.upb.chatupb_v2.Model.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class ConnectionDB {

    private static final String URL = "jdbc:sqlite:chat_upb_v2.sqlite";
    private static final ConnectionDB connection = new ConnectionDB();
    
    private ConnectionDB(){
        inicializarBaseDeDatos();
    }
    
    public static ConnectionDB getInstance(){
        return connection;
    }

    public Connection getConection(){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);
            if (conn != null) {
            } else {
                System.out.println("Conexión fallida");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch(ClassNotFoundException e){}
        return conn;   
    }

    private static String generarUrlBaseDeDatos() {

        String appData = System.getenv("APPDATA");
        File carpetaApp;

        if (appData != null) {

            carpetaApp = new File(appData, "ChatUPB");
        } else {

            carpetaApp = new File(System.getProperty("user.home"), ".ChatUPB");
        }

        if (!carpetaApp.exists()) {
            carpetaApp.mkdirs();
        }

        File archivoDb = new File(carpetaApp, "chat_upb_v2.sqlite");

        return "jdbc:sqlite:" + archivoDb.getAbsolutePath().replace("\\", "/");
    }
    private void inicializarBaseDeDatos() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            String sqlUsers = "CREATE TABLE IF NOT EXISTS Users (\n"
                    + " id TEXT PRIMARY KEY,\n"
                    + " name TEXT,\n"
                    + " ip_user TEXT,\n"
                    + " saved_by_id TEXT\n"
                    + ");";

            String sqlAccounts = "CREATE TABLE IF NOT EXISTS Accounts (\n"
                    + " id_account TEXT PRIMARY KEY,\n"
                    + " nombre TEXT\n "
                    + ");";

            String sqlMessages = "CREATE TABLE IF NOT EXISTS Messages (\n"
                    + " id_message TEXT PRIMARY KEY,\n"
                    + " sender_id TEXT,\n"
                    + " receiver_id TEXT,\n"
                    + " body TEXT,\n"
                    + " type_message TEXT,\n"
                    + " status_message TEXT,\n"
                    + " date TEXT,\n"
                    + " FOREIGN KEY (sender_id) REFERENCES Users(id),\n"
                    + " FOREIGN KEY (receiver_id) REFERENCES Users(id)\n"
                    + ");";

            stmt.execute(sqlUsers);
            stmt.execute(sqlAccounts);
            stmt.execute(sqlMessages);

            System.out.println("Base de datos y tablas inicializadas correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al inicializar las tablas: " + e.getMessage());
        }
    }
}
