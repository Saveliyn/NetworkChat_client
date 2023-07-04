package lesson7.server;

import lesson7.server.handlers.ClientHandler;
import lesson7.server.services.AuthenticationService;
import lesson7.server.services.impl.SimpleAuthenticationServiceImpl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

public class MyServer {

    private final ServerSocket serverSocket;
    private final AuthenticationService authenticationService;
    private final ArrayList<ClientHandler> clients;
    public ArrayList<String> online;
    public static Connection connection;
    public static Statement stmt;
    private BufferedReader reader;
    private int lines;

    public final Logger logger = Logger.getLogger("log");

    public MyServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        authenticationService = new SimpleAuthenticationServiceImpl();
        clients = new ArrayList<>();
        online = new ArrayList<>();
        Handler handler = new FileHandler("src/main/resources/configs/log.txt");
        logger.addHandler(handler);
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("%s\t%s\t%s%n",
                        record.getLevel(),
                        record.getMessage(),
                        record.getSourceClassName());
            }
        });
    }




    public void start() {
        logger.info("CЕРВЕР ЗАПУЩЕН!");
        //System.out.println("CЕРВЕР ЗАПУЩЕН!");
        System.out.println("---------------");

        try {
            while (true) {
                waitAndProcessNewClientConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitAndProcessNewClientConnection() throws IOException {
        logger.info("Ожидание клиента...");
        //System.out.println("Ожидание клиента...");
        Socket socket = serverSocket.accept();
        logger.info("Клиент подключился!");
        //System.out.println("Клиент подключился!");

        processClientConnection(socket);
    }

    private void processClientConnection(Socket socket) throws IOException {
        ClientHandler handler = new ClientHandler(this, socket);
        handler.handle();
    }

    public synchronized void subscribe(ClientHandler handler) {
        clients.add(handler);
        online.add(handler.getUsername());
    }

    public synchronized void unSubscribe(ClientHandler handler) {

        clients.remove(handler);
        online.remove(handler.getUsername());

    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void stop() throws SQLException {
        System.out.println("------------------");
        System.out.println("------------------");
        logger.info("ЗАВЕРШЕНИЕ РАБОТЫ");
        //System.out.println("ЗАВЕРШЕНИЕ РАБОТЫ");
        disconnectionDB();
        System.exit(0);
    }

    public void disconnectionDB() throws SQLException {
        connection.close();
    }

    public synchronized void broadcastMessage(ClientHandler sender, String message) throws IOException {
        saveHistory(sender.getUsername(),message);
        for (ClientHandler client : clients) {
            client.sendMessage(sender.getUsername(), message);
        }
    }

    private void saveHistory(String username, String message) {
        try {
            File history = new File("history.txt");
            PrintWriter fileWriter = new PrintWriter(new FileWriter(history, true));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            BufferedReader reader = new BufferedReader(new FileReader(history));
            while (reader.readLine() != null) lines++;
            //System.out.println(lines);

            bufferedWriter.write(String.format("%s: %s \n", username, message));

            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void sendPrivateMessage(ClientHandler sender, String recipient, String privateMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendMessage(sender.getUsername(), privateMessage, true);
                privateMessage = String.format("[pm] для %s: %s ", recipient, privateMessage);
                sender.sendMessage(sender.getUsername(), privateMessage);
            }
        }
    }

    public synchronized void sendChatHistory(String recipient, String privateMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendHistory(privateMessage);
            }
        }
    }

    public synchronized void broadcastServerMessage(ClientHandler sender, String message) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendServerMessage(message);
        }
    }

    public synchronized void addUsers(ClientHandler sender, String username) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendAddUsersMessage(username);
        }
    }

    public synchronized void removeUser(ClientHandler sender, String username) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendRemoveUsersMessage(username);
        }
    }
}
