/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.model.repository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionDB {

    private static final ConnectionDB connection = new ConnectionDB();

    private static final String URL = "jdbc:sqlite:chat_upb_v2.sqlite";

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

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                System.out.println("Conexión exitosa");
            } else {
                System.out.println("Conexión fallida.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return conn;
    }

    private void inicializarBaseDeDatos() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // 1. Tabla Users
            String sqlUsers = "CREATE TABLE IF NOT EXISTS Users(\n"
                    + " id TEXT PRIMARY KEY,\n"
                    + " name TEXT UNIQUE\n"
                    + ");";

            // 2. Tabla Contacts
            String sqlContacts = "CREATE TABLE IF NOT EXISTS Contacts (\n"
                    + " id TEXT PRIMARY KEY,\n"
                    + " Users_id TEXT,\n"
                    + " name TEXT NOT NULL,\n"
                    + " ip TEXT NOT NULL,\n"
                    + " FOREIGN KEY (Users_id) REFERENCES Users(id) ON DELETE CASCADE\n"
                    + ");";

            // 3. Tabla Messages
            String sqlMessages = "CREATE TABLE IF NOT EXISTS Messages (\n"
                    + " id TEXT PRIMARY KEY,\n"
                    + " Contacts_id TEXT,\n"
                    + " message TEXT NOT NULL,\n"
                    + " date TEXT,\n"
                    + " type TEXT,\n"
                    + " status TEXT,\n"
                    + " FOREIGN KEY (Contacts_id) REFERENCES Contacts(id) ON DELETE CASCADE\n"
                    + ");";


            stmt.execute(sqlUsers);
            stmt.execute(sqlContacts);
            stmt.execute(sqlMessages);

            System.out.println("Base de datos y tablas inicializadas correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al inicializar las tablas: " + e.getMessage());
        }
    }
}
