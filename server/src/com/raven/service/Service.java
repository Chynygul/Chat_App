package com.raven.service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
//import com.mysql.jdbc.PreparedStatement;
import com.raven.app.MessageType;
import com.raven.model.Model_Client;
import com.raven.model.Model_File;
import com.raven.model.Model_Login;
import com.raven.model.Model_Message;
import com.raven.model.Model_Package_Sender;
import com.raven.model.Model_Receive_Image;
import com.raven.model.Model_Receive_Message;
import com.raven.model.Model_Register;
import com.raven.model.Model_Reques_File;
import com.raven.model.Model_Send_Message;
import com.raven.model.Model_User_Account;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;

public class Service {
    
    private static Service instance;
    private SocketIOServer server;
    private ServiceUser serviceUser;
    private ServiceFIle serviceFile;
    private List<Model_Client> listClient;
    private JTextArea textArea;
    private final int PORT_NUMBER = 9999;
    private ServiceMessage serviceMessage;
    
    public static Service getInstance(JTextArea textArea) {
        if (instance == null) {
            instance = new Service(textArea);
        }
        return instance;
    }

    private Service(JTextArea textArea) {
        this.textArea = textArea;
        serviceUser = new ServiceUser();
        serviceFile = new ServiceFIle();
        listClient = new ArrayList<>();
        serviceMessage = new ServiceMessage();
    }
    
    public void startServer() {
        Configuration config = new Configuration();
        config.setPort(PORT_NUMBER);
        server = new SocketIOServer(config);
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient sioc) {
                textArea.append("One client connected\n");
            }
        });
        server.addEventListener("register", Model_Register.class, new DataListener<Model_Register>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Register t, AckRequest ar) throws Exception {
                Model_Message message = serviceUser.register(t);
                ar.sendAckData(message.isAction(), message.getMessage(), message.getData());
                if (message.isAction()) {
                    textArea.append("User has Register :" + t.getUserName() + " Pass :" + t.getPassword() + "\n");
                    server.getBroadcastOperations().sendEvent("list_user", (Model_User_Account) message.getData());
                    addClient(sioc, (Model_User_Account) message.getData());
                }
            }
        });
        server.addEventListener("login", Model_Login.class, new DataListener<Model_Login>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Login t, AckRequest ar) {
                try {
                    Model_Message result = serviceUser.loginWithReason(t);

                    if (!result.isAction()) {
                        // false + reason: USER_NOT_FOUND / EMAIL_NOT_MATCH / WRONG_PASSWORD
                        ar.sendAckData(false, result.getMessage());
                        return;
                    }

                    Model_User_Account login = (Model_User_Account) result.getData();

                    // ✅ 1) СНАЧАЛА ACK (чтобы клиент не зависал никогда)
                    ar.sendAckData(true, login);

                    // ✅ 2) потом уже добавляем/обновляем клиента
                    upsertClient(sioc, login);

                    // ✅ 3) дальше все события можно слать отдельно (они не должны ломать логин)
                    try {
                        List<Model_User_Account> users = serviceUser.getUser(login.getUserID());
                        sioc.sendEvent("list_user", users.toArray());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // статусы онлайн
                    try {
                        for (Model_Client c : listClient) {
                            int uid = c.getUser().getUserID();
                            if (uid != login.getUserID()) {
                                sioc.sendEvent("user_status", uid, true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // broadcast что этот юзер онлайн
                    userConnect(login.getUserID());

                } catch (Exception e) {
                    e.printStackTrace();
                    ar.sendAckData(false, "SERVER_ERROR");
                }
            }
        });
        server.addEventListener("list_user", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient sioc, Integer userID, AckRequest ar) throws Exception {
                try {
                    List<Model_User_Account> list = serviceUser.getUser(userID);
                    sioc.sendEvent("list_user", list.toArray());
                } catch (SQLException e) {
                    System.err.println(e);
                }
            }
        });
        server.addEventListener("send_to_user", Model_Send_Message.class, new DataListener<Model_Send_Message>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Send_Message t, AckRequest ar) throws Exception {
                sendToClient(t, ar);
                System.out.println("FILE NAME: " + t.getFileName());
                System.out.println("FILE SIZE: " + t.getFileSize());
            }
        });
        server.addEventListener("send_file", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Package_Sender t, AckRequest ar) throws Exception {
                try {
                    serviceFile.receiveFile(t);
                    if (t.isFinish()) {
                        ar.sendAckData(true);
                        Model_Receive_Image dataImage = new Model_Receive_Image();
                        dataImage.setFileID(t.getFileID());
                        Model_Send_Message message = serviceFile.closeFile(dataImage);
                        //  Send to client 'message'
                        sendTempFileToClient(message, dataImage);
                        
                    } else {
                        ar.sendAckData(true);
                    }
                } catch (IOException | SQLException e) {
                    ar.sendAckData(false);
                    e.printStackTrace();
                }
            }
        });
        server.addEventListener("get_file", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient sioc, Integer t, AckRequest ar) throws Exception {
                Model_File file = serviceFile.initFile(t);
                long fileSize = serviceFile.getFileSize(t);
                ar.sendAckData(file.getFileExtension(), fileSize);
            }
        });
        server.addEventListener("reques_file", Model_Reques_File.class, new DataListener<Model_Reques_File>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Reques_File t, AckRequest ar) throws Exception {
                byte[] data = serviceFile.getFileData(t.getCurrentLength(), t.getFileID());
                if (data != null) {
                    ar.sendAckData(data);
                } else {
                    ar.sendAckData();
                }
            }
        });
//        server.addEventListener("load_chat", Integer[].class, new DataListener<Integer[]>() {
//            @Override
//            public void onData(SocketIOClient sioc, Integer[] users, AckRequest ar) throws Exception {

        server.addEventListener("load_chat", List.class, new DataListener<List>() {
            @Override
            public void onData(SocketIOClient sioc, List users, AckRequest ar) throws Exception {

                System.out.println("=== load_chat event received ===");
                System.out.println("Client ID: " + sioc.getSessionId());
                System.out.println("Users array length: " + (users == null ? "null" : users.size()));

                if (users == null || users.size() < 2) {
                    System.out.println("ERROR: Invalid users array");
                    ar.sendAckData(); // Отправляем пустой ответ
                    return;
                }


                try {
                    int user1 = (Integer) users.get(0);
                    int user2 = (Integer) users.get(1);
                    System.out.println("User1: " + user1 + ", User2: " + user2);

                    System.out.println("Loading chat history from DB...");
                    List<Model_Receive_Message> history = serviceMessage.getChatHistory(user1, user2);

                    System.out.println("History size from DB: " + history.size()); // ВАЖНО!

                    if (history.isEmpty()) {
                        System.out.println("No messages found in DB for users: " + user1 + ", " + user2);
                        ar.sendAckData(new Object[0]); // Отправляем пустой массив
                        return;
                    }

                    List<Object> list = new ArrayList<>();
                    System.out.println("list for client(result)" + list);

                    System.out.println("Конвертация starting");
                    for (Model_Receive_Message msg : history) {
                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                        map.put("messageType", msg.getMessageType());
                        map.put("fromUserID", msg.getFromUserID());
                        map.put("text", msg.getText() != null ? msg.getText() : "");
                        if (msg.getFileName() != null) {
                            map.put("fileName", msg.getFileName());
                        }
                        map.put("fileSize", msg.getFileSize());
                        map.put("fileID", msg.getFileID());
                        map.put("sentAt", msg.getSentAt());

                        if (msg.getMessageType() == MessageType.IMAGE.getValue() && msg.getFileID() > 0) {
                            try {
                                Model_Receive_Image preview = serviceFile.getImagePreviewForHistory(msg.getFileID());
                                if (preview.getImage() != null && !preview.getImage().isEmpty()) {
                                    java.util.Map<String, Object> di = new java.util.HashMap<>();
                                    di.put("fileID", preview.getFileID());
                                    di.put("image", preview.getImage());
                                    di.put("width", preview.getWidth());
                                    di.put("height", preview.getHeight());
                                    map.put("dataImage", di);
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                        list.add(map);
                    }
                    System.out.println("Отправка ответа клиенту: ");
                    System.out.println(list.toArray());
                    ar.sendAckData(list.toArray());
                } catch (Exception e) {
                    System.err.println("SQL Error in getChatHistory: " + e.getMessage());
                    e.printStackTrace();
                    ar.sendAckData();
                }
            }
        });
        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient sioc) {
                int userID = removeClient(sioc);
                if (userID != 0) {
                    //  removed
                    userDisconnect(userID);
                }
            }
        });
        server.start();
        textArea.append("Server has Start on port : " + PORT_NUMBER + "\n");
    }

    private void upsertClient(SocketIOClient client, Model_User_Account user) {
        // если этот сокет уже есть в списке, просто обновим user
        for (Model_Client c : listClient) {
            if (c.getClient() == client) {
                c.setUser(user); // если у Model_Client нет setUser, тогда удалим и добавим
                return;
            }
        }

        // если пользователь уже был онлайн с другого сокета, можно заменить (по желанию)
        for (int i = 0; i < listClient.size(); i++) {
            if (listClient.get(i).getUser().getUserID() == user.getUserID()) {
                listClient.set(i, new Model_Client(client, user));
                return;
            }
        }

        listClient.add(new Model_Client(client, user));
    }

    private void userConnect(int userID) {
        server.getBroadcastOperations().sendEvent("user_status", userID, true);
    }
    
    private void userDisconnect(int userID) {
        server.getBroadcastOperations().sendEvent("user_status", userID, false);
    }
    
    private void addClient(SocketIOClient client, Model_User_Account user) {
        listClient.add(new Model_Client(client, user));
    }

    private void sendToClient(Model_Send_Message data, AckRequest ar) {
        if (data.getMessageType() == MessageType.IMAGE.getValue() || data.getMessageType() == MessageType.FILE.getValue()) {
            try {
                Model_File file = serviceFile.addFileReceiver(data.getText());
                serviceFile.initFile(file, data);
                ar.sendAckData(file.getFileID());
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } else {
            Model_Receive_Message msg = new Model_Receive_Message(
                    data.getMessageType(),
                    data.getFromUserID(),
                    data.getText(),
                    null,
                    data.getSentAt()
            );

            // СНАЧАЛА сохраняем в БД
            try {
                serviceMessage.saveMessage(msg, data.getToUserID());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // ПОТОМ пытаемся доставить онлайн-получателю
            for (Model_Client c : listClient) {
                if (c.getUser().getUserID() == data.getToUserID()) {
                    c.getClient().sendEvent("receive_ms", msg);
                    //c.getClient().sendEvent("load_chat", msg);
                    break;
                    //serviceMessage.getChatHistory(c.getUser().getUserID(), data.getToUserID());
                }
            }
        }
    }

    private void sendTempFileToClient(Model_Send_Message data, Model_Receive_Image dataImage) {
        for (Model_Client c : listClient) {
            if (c.getUser().getUserID() == data.getToUserID()) {

                Model_Receive_Message ms = new Model_Receive_Message(
                        data.getMessageType(),
                        data.getFromUserID(),
                        data.getText(),
                        dataImage,
                        data.getSentAt()
                );

                ms.setFileName(data.getFileName());
                ms.setFileSize(data.getFileSize());
                ms.setFileID(dataImage.getFileID());

                c.getClient().sendEvent("receive_ms", ms);
                //c.getClient().sendEvent("load_chat", ms);

                try {
                    serviceMessage.saveMessage(ms, data.getToUserID());
                } catch (SQLException e) {
                    System.out.println(" Error from sendTempFileToClient from Service from server: ");
                    e.printStackTrace();
                }

                break;
            }
        }
    }
    
    public int removeClient(SocketIOClient client) {
        for (Model_Client d : listClient) {
            if (d.getClient() == client) {
                listClient.remove(d);
                return d.getUser().getUserID();
            }
        }
        return 0;
    }
    
    public List<Model_Client> getListClient() {
        return listClient;
    }
}
