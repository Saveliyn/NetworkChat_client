package lesson7.server.services.impl;

import lesson7.server.MyServer;
import lesson7.server.models.User;
import lesson7.server.services.AuthenticationService;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleAuthenticationServiceImpl implements AuthenticationService {

    /*public static  List<User> users = List.of(
            new User("martin", "1", "Martin_Superstar"),
            new User("batman", "1", "Брюс_Уэйн"),
            new User("gena", "1", "Гендальф_Серый"),
            new User("mario", "1", "Super_Mario"),
            new User("bender", "1", "Bender"),
            new User("ezhik", "1", "Super_Sonic")
    );
    public static ArrayList<User> clients = new ArrayList<>(List.copyOf(users));*/
/*
    public static void registrationNewUser(User user) {
        clients = new ArrayList<>(List.copyOf(users));
        clients.add(user);
    }


    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User client : clients) {
            if (client.getLogin().equals(login) && client.getPassword().equals(password) ) {
                return client.getUsername();
            }
        }
        return null;
    }*/
    public static Connection connection;
    public static Statement stmt;

    public static void updateUsername(String username, String newUsername) {
            try {
                connectionDB();
                stmt.executeUpdate(String.format("UPDATE auth1 SET username = '%s' WHERE login = '%s' ", newUsername, getLoginByUsername(username)));
            } catch (SQLException e) {
                e.printStackTrace();
                
            } catch (ClassNotFoundException e) {
                e.printStackTrace();

            }
    }

    private static String getLoginByUsername(String username) throws SQLException {
        ResultSet rs = stmt.executeQuery(String.format("SELECT * from auth1 WHERE username = '%s' ", username));
        System.out.println(rs.getString("login"));
        return rs.getString("login");
    }

    public static void addNewUser(String login, String password, String username) {
        int y =1;
        int b = 2;
        try {
            connectionDB();
            stmt.executeUpdate(String.format("INSERT INTO auth1 (login, password, username) VALUES ('%s', '%s', '%s')", login,
                    password, username));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("y=" + y + " " + "b= " + b);
    }



    private static void write(int y, Object b) {
        y =2;
        b = "c";
    }



















    public String getUsernameByLoginAndPassword(String login, String password) throws SQLException, ClassNotFoundException {
        connectionDB();
        ResultSet rs = stmt.executeQuery(String.format("SELECT * from auth1 WHERE login = '%s' ", login));
        if(rs.isClosed()){
            rs.isClosed();
            return null;
        }

        String usernameDB = rs.getString("username");
        String passwordDB = rs.getString("password");



        connection.close();
        return ((passwordDB != null) && (passwordDB.equals(password)) ? usernameDB : null);
    }

    public static void connectionDB() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/db/auth1.db");
        stmt = connection.createStatement();
    }


}
