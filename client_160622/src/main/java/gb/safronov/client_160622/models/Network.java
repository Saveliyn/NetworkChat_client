package gb.safronov.client_160622.models;

import gb.safronov.client_160622.StartClient;
import gb.safronov.client_160622.controllers.ChatController;
import javafx.application.Platform;
import javafx.collections.FXCollections;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Network {
    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + username + msg
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";
    private static final String NEW_USER_CMD_PREFIX = "/reg";
    private static final String REGOK_CMD_PREFIX = "/regok";
    private static final String LIST_CMD_PREFIX = "/on";
    private static final String LIST_OUT_CMD_PREFIX = "/of";
    private static final String UPDATE_USERNAME_CMD_PREFIX = "/usr";
    private static final String LOAD_CMD_PREFIX = "/loadh";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8888;
    //ВНИМАНИЕ! ПЕРЕЗАГРУЗКА СЕРВЕРА ЧЕРЕЗ 15 МИНУТ
    private final String host;
    private final int port;
    private DataOutputStream out;
    private DataInputStream in;
    private ChatController chatController;
    private String username;
    private StartClient startClient;
    public ArrayList<String> online;

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Network() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public void connect() {
        try {
            Socket socket = new Socket(host, port);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());


        } catch (IOException e) {
            e.printStackTrace();
            startClient.showErrorAlert("Ошибка подключения","Соединение не установлено");
//            start
        }
    }


    public void sendMessage(String message) {
        try {
            out.writeUTF(String.format("%s %s", CLIENT_MSG_CMD_PREFIX, message));
        } catch (IOException e) {
            e.printStackTrace();
            startClient.showErrorAlert("Ошибка подключения","Соединение не установлено");

        }
    }

    public void sendPrivateMessage(String recipient, String message) {
        try {
            out.writeUTF(String.format("%s %s %s", PRIVATE_MSG_CMD_PREFIX, recipient, message));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при отправке сообщения");
        }
    }

    public String sendRegMessage(String login, String password, String username) {
        try {
            out.writeUTF(String.format("%s %s %s %s", NEW_USER_CMD_PREFIX, login, password, username));
            online = new ArrayList<>();
            while (true) {
                String answer = in.readUTF();
                if(answer.startsWith(LIST_CMD_PREFIX)) {
                    online.add(answer.split("\\s+", 2)[1]);
                } else if (answer.startsWith(REGOK_CMD_PREFIX)) {
                    return null;
                } else return "Ошибка";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    public String sendAuthMessage(String login, String password) {
        try {
            out.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            online = new ArrayList<>();
           while (true) {
                String response = in.readUTF();
                if(response.startsWith(LIST_CMD_PREFIX)) {
                    online.add(response.split("\\s+", 2)[1]);
                }
                else if (response.startsWith(AUTHOK_CMD_PREFIX)) {
                    this.username = response.split("\\s+", 2)[1];
                    return null;
                }  else {
                    return response.split("\\s+", 2)[1];
                }
            }
            } catch(IOException e){
                e.printStackTrace();
                System.out.println("Ошибка при отправке сообщения");
                return e.getMessage();
            }
    }

    public void waitMessage(ChatController chatController) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();

                    String typeMessage = message.split("\\s+")[0];
                    if (!typeMessage.startsWith("/")) {
                        System.out.println("Неверный запрос");
                    }

                    switch (typeMessage) {
                        case CLIENT_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 3);
                            String sender = parts[1];
                            String messageFromSender = parts[2];

                            if (sender.equals(username)) {
                                sender = this.username;
                            }

                            String finalSender = sender;
                            Platform.runLater(() -> chatController.appendMessage(finalSender, messageFromSender));
                            //chatController.saveHistory(finalSender, messageFromSender);
                        }
                        case LIST_CMD_PREFIX -> {
                            online.add(message.split("\\s+", 2)[1]);
                            chatController.addUserToListView(online);
                            chatController.usersList.refresh();
                        }

                        case LIST_OUT_CMD_PREFIX -> {
                            online.remove(message.split("\\s+", 2)[1]);
                            chatController.addUserToListView(online);
                            chatController.usersList.refresh();
                        }
                        case PRIVATE_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 3);
                            String sender = parts[1];
                            String messageFromSender = parts[2];

                            Platform.runLater(() -> chatController.appendMessage("[pm]" + sender, messageFromSender));
                        }
                        case LOAD_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 2);
                            chatController.appendHistory(parts[1]);
                        }
                        case SERVER_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 2);
                            String serverMessage = parts[1];

                            chatController.appendServerMessage(serverMessage);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        t.setDaemon(true);
        t.start();
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public String getUsername() {
        return username;
    }

    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }

    public StartClient getStartClient() {
        return startClient;
    }

    public void changeUsernameMessage(String message) {
        try {
            out.writeUTF(String.format("%s %s", UPDATE_USERNAME_CMD_PREFIX, message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
