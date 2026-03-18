package edu.upb.chatupb_v2.model.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionDB {

    // 1. Primero generamos la URL dinámica (Debe ir ANTES de la instancia 'connection')
    private static final String URL = generarUrlBaseDeDatos();

    // 2. Luego inicializamos la instancia (así la URL ya existe cuando se llama al constructor)
    private static final ConnectionDB connection = new ConnectionDB();

    private ConnectionDB() {
        inicializarBaseDeDatos();
    }

    public static ConnectionDB getInstance() {
        return connection;
    }

    private static String generarUrlBaseDeDatos() {

        String appData = System.getenv("APPDATA");
        File carpetaApp;

        if (appData != null) {

            carpetaApp = new File(appData, "ChatUPB");
        } else {

            carpetaApp = new File(System.getProperty("user.home"), ".ChatUPB");
        }

        // Si la carpeta no existe, la creamos
        if (!carpetaApp.exists()) {
            carpetaApp.mkdirs();
        }

        // Apuntamos al archivo SQLite dentro de esa carpeta
        File archivoDb = new File(carpetaApp, "chat_upb_v2.sqlite");

        // SQLite requiere que la ruta esté en formato de URL, así que reemplazamos las barras
        return "jdbc:sqlite:" + archivoDb.getAbsolutePath().replace("\\", "/");
    }

    public Connection getConection() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);

            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                System.out.println("✅ Conexión exitosa a la BD en: " + URL);
            } else {
                System.out.println("❌ Conexión fallida.");
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
                    + " id_pin TEXT NOT NULL,\n"
                    + " theme INT,\n"
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