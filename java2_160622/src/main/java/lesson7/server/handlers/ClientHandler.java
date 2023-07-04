package lesson7.server.handlers;

import lesson7.server.MyServer;
import lesson7.server.models.User;
import lesson7.server.services.AuthenticationService;
import lesson7.server.services.impl.SimpleAuthenticationServiceImpl;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ClientHandler {
    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + username + msg
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";
    private static final String LIST_CMD_PREFIX = "/on";
    private static final String LIST_OUT_CMD_PREFIX = "/of";
    private static final String NEW_USER_CMD_PREFIX = "/reg";
    private static final String REGOK_CMD_PREFIX = "/regok";
    private static final String LOAD_CMD_PREFIX = "/loadh";

    private static final String UPDATE_USERNAME_CMD_PREFIX = "/usr";
    private MyServer myServer;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private int lines;


    public ClientHandler(MyServer myServer, Socket socket) {

        this.myServer = myServer;
        clientSocket = socket;
    }


    public void handle() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
//        ObjectInputStream inobj = new ObjectInputStream(clientSocket.getInputStream());
//        inobj.readObject();
        out = new DataOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                checkMessage();
                readMessage();
            } catch (IOException | SQLException e) {
                try {
                    myServer.broadcastServerMessage(this, "Пользователь " + username + " отключился от чата");
                    myServer.removeUser(this, username);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                myServer.unSubscribe(this);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void checkMessage() throws IOException, SQLException, ClassNotFoundException {
        while(true) {
            String message = in.readUTF();
            System.out.println(message);
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                authentication(message);
                break;
            } else if (message.startsWith(NEW_USER_CMD_PREFIX)) {
                registration(message);
                break;
            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
                myServer.logger.warning("Неверная команда аутентификации");
                //System.out.println("Неверная команда аутентификации");
                break;
            }
        }
    }


    private void registration(String message) throws IOException, SQLException, ClassNotFoundException {
        String[] parts = message.split("\\s+");
        //User user = new User(parts[1], parts[2], parts[3] );
        //SimpleAuthenticationServiceImpl.registrationNewUser(user);
        SimpleAuthenticationServiceImpl.addNewUser(parts[1], parts[2], parts[3]);
        AuthenticationService auth = myServer.getAuthenticationService();

        username = auth.getUsernameByLoginAndPassword(parts[1], parts[2]);
        myServer.subscribe(this);
        for (int i = 0; i < myServer.online.toArray().length; i++) {
            out.writeUTF(LIST_CMD_PREFIX + " " + myServer.online.toArray()[i].toString());
        }
        out.writeUTF(REGOK_CMD_PREFIX + " " + parts[3]);
        myServer.logger.info(String.format("Пользователь %s подключился к чату", username));
       // System.out.println("Пользователь " + username + " подключился к чату");
        myServer.addUsers(this, username);
        myServer.broadcastServerMessage(this, "Пользователь " + username + " подключился к чату");

    }

    private void authentication(String message) throws IOException, SQLException, ClassNotFoundException {
       /* while (true) {
            String message = in.readUTF();
            System.out.println(message);*/

        if (message.startsWith(AUTH_CMD_PREFIX)) {
            boolean isSuccessAuth = processAuthentication(message);
            if (isSuccessAuth) {
                //break;
            }
        }
        }

    private boolean processAuthentication(String message) throws IOException, SQLException, ClassNotFoundException {
        String[] parts = message.split("\\s+");
        if (parts.length != 3) {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
            myServer.logger.warning("Неверная команда аутентификации");
            //System.out.println("Неверная команда аутентификации");
            return false;
        }

        String login = parts[1];
        String password = parts[2];

        AuthenticationService auth = myServer.getAuthenticationService();

        username = auth.getUsernameByLoginAndPassword(login, password);

        if (username != null) {
           /* if (myServer.isUsernameBusy(username)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин уже используется");
                return false;
            }*/

            myServer.subscribe(this);
            for (int i = 0; i < myServer.online.toArray().length; i++) {
                out.writeUTF(LIST_CMD_PREFIX + " " + myServer.online.toArray()[i].toString());
            }
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            myServer.logger.info(String.format("Пользователь %s подключился к чату", username));
            //System.out.println("Пользователь " + username + " подключился к чату");
            loadHistory();
            myServer.addUsers(this, username);
            myServer.broadcastServerMessage(this, "Пользователь " + username + " подключился к чату");

            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная комбинация логина и пароля");
            return false;
        }
    }

    private void loadHistory() throws IOException {
        ArrayList<String> chatHistory = new ArrayList<>();
        File history = new File("history.txt");
        BufferedReader reader = new BufferedReader(new FileReader(history));
        String b;
        while ((b = reader.readLine())!=null){
            lines++;
            chatHistory.add(b);
        }
        if(chatHistory.size()<=100){
            for (int i = 0; i < chatHistory.size(); i++) {
                myServer.sendChatHistory(this.username, chatHistory.get(i));
            }
        } else {
            for (int i = (chatHistory.size() - 100); i < chatHistory.size(); i++) {;
                myServer.sendChatHistory(this.username, chatHistory.get(i));
            }
        }
    }

    private void readMessage() throws IOException, SQLException, ClassNotFoundException {
        while (true) {
            String message = in.readUTF();

            //System.out.println("message | " + username + ": " + message);
            myServer.logger.info(String.format("message | %s %s", username, message));

            String typeMessage = message.split("\\s+")[0];
            if (!typeMessage.startsWith("/")) {
                myServer.logger.warning("Неверный запрос!");
            }


            switch (typeMessage) {
                case STOP_SERVER_CMD_PREFIX -> myServer.stop();
                case END_CLIENT_CMD_PREFIX -> closeConnection();
                case CLIENT_MSG_CMD_PREFIX -> {
                    String[] messageParts = message.split("\\s+", 2);
                    myServer.broadcastMessage(this, messageParts[1]);}
                case UPDATE_USERNAME_CMD_PREFIX ->  {
                    String[] messageParts = message.split("\\s+", 2);
                    SimpleAuthenticationServiceImpl.updateUsername(this.getUsername(), messageParts[1]);

                }
                case PRIVATE_MSG_CMD_PREFIX -> {
                    String[] privateMessageParts = message.split("\\s+", 3);
                    String recipient  = privateMessageParts[1];
                    String privateMessage  = privateMessageParts[2];

                    myServer.sendPrivateMessage(this, recipient, privateMessage);
                } default -> myServer.logger.warning("Неверная команда!");
            }

        }
    }

    private void closeConnection() throws IOException {
        clientSocket.close();
        myServer.logger.info(String.format("Пользователь %s отключился от чата", username));
    }

    public void sendServerMessage(String message) throws IOException {
        out.writeUTF(String.format("%s %s", SERVER_MSG_CMD_PREFIX, message));
    }

    public void sendAddUsersMessage(String username) throws IOException {
        out.writeUTF(String.format("%s %s", LIST_CMD_PREFIX, username));
    }


    public void sendMessage(String sender, String message, Boolean isPrivate) throws IOException {
        out.writeUTF(String.format("%s %s %s", isPrivate ?
                PRIVATE_MSG_CMD_PREFIX
                : CLIENT_MSG_CMD_PREFIX,
                sender, message));
    }

    public void sendMessage(String sender, String message) throws IOException {
        sendMessage(sender, message, false);

    }

    public String getUsername() {
        return username;
    }

    public void sendRemoveUsersMessage(String username) throws IOException {
        out.writeUTF(String.format("%s %s", LIST_OUT_CMD_PREFIX, username));
    }

    public void sendHistory(String privateMessage) throws IOException {
        out.writeUTF(String.format("%s %s", LOAD_CMD_PREFIX, privateMessage));
    }
}
