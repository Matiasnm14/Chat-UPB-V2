package edu.upb.chatupb_v2.model.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

/**
 *
 * @author rlaredo
 */
public class ConnectionDB {

    private static final ConnectionDB connection = new ConnectionDB();

    // 1. Usamos el método para que la URL se calcule automáticamente al iniciar el programa
    private static final String URL = getDatabaseURL();

    private ConnectionDB(){
    }

    // 2. Convertimos la lógica en un método privado estático de esta misma clase
    private static String getDatabaseURL() {
        String appDataPath = System.getenv("APPDATA");
        File folder = new File(appDataPath + File.separator + "ChatUPB");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String dbPath = folder.getAbsolutePath() + File.separator + "chat_upb_v2.sqlite";
        return "jdbc:sqlite:" + dbPath;
    }

    public static ConnectionDB getInstance(){
        return connection;
    }

    public Connection getConection(){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");

            // 3. Reemplazamos el texto fijo por nuestra variable URL dinámica
            conn = DriverManager.getConnection(URL);

            if (conn != null) {
                try(Statement stm = conn.createStatement()){
                    stm.execute("PRAGMA foreign_keys = ON");
                }
                // Añadí la URL al print para que veas exactamente dónde se está guardando cuando depures
                System.out.println("Conexión exitosa a: " + URL);
            } else {
                System.out.println("Conexión fallida");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return conn;
    }

    public static void initDatabase() {
        // Este método ya usaba la variable URL, así que ahora usará la de AppData automáticamente
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS Contacts (" +
                    "id TEXT PRIMARY KEY, " +
                    "Users_id TEXT, " +
                    "name TEXT, " +
                    "ip TEXT, " +
                    "id_theme TEXT," +
                    "id_pin TEXT"+
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS Messages (" +
                    "id TEXT PRIMARY KEY, " +
                    "Contacts_id TEXT, " +
                    "message TEXT, " +
                    "date TEXT, " +
                    "type TEXT, " +
                    "status TEXT" +
                    ");");

            System.out.println("Base de datos verificada/creada con éxito.");
        } catch (Exception e) {
            System.out.println("Error inicializando la base de datos: " + e.getMessage());
        }
    }
}