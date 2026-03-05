/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.model.repository;

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
    private static final String URL = "jdbc:sqlite:chat_upb_v2.sqlite";
    
    private ConnectionDB(){
       
    }
    
    public static ConnectionDB getInstance(){
        return connection;
    }

    
    public Connection getConection(){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:chat_upb_v2.sqlite");
            if (conn != null) {
                try(Statement stm = conn.createStatement()){
                    stm.execute("PRAGMA foreign_keys = ON");
                }
                System.out.println("Conexión exitosa.");
            } else {
                System.out.println("Conexión fallida");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }catch(ClassNotFoundException e){
        
        }
        return conn;   
    }

    public static void initDatabase() {
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
                    "ip TEXT " +
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
