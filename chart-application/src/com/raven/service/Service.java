package com.raven.service;

import com.raven.event.EventFileReceiver;
import com.raven.event.PublicEvent;
import com.raven.model.Model_File_Receiver;
import com.raven.model.Model_File_Sender;
import com.raven.model.Model_Receive_Message;
import com.raven.model.Model_Send_Message;
import com.raven.model.Model_User_Account;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Service {

    private static Service instance;
    private Socket client;
    private final int PORT_NUMBER = 9999;
    private final String IP = "localhost";
    private Model_User_Account user;
    private List<Model_File_Sender> fileSender;
    private List<Model_File_Receiver> fileReceiver;

    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    private Service() {
        fileSender = new ArrayList<>();
        fileReceiver = new ArrayList<>();
    }

    public void startServer() {
        try {
            client = IO.socket("http://" + IP + ":" + PORT_NUMBER);

            client.on(Socket.EVENT_CONNECT, args -> System.out.println("Connected"));
            client.on(Socket.EVENT_CONNECT_ERROR, args -> {
                System.err.println("Connect error: " + (args.length > 0 ? args[0] : ""));
                PublicEvent.getInstance().getEventMain().showLoading(false);
            });
            client.on(Socket.EVENT_DISCONNECT, args -> {
                System.err.println("Disconnected");
                PublicEvent.getInstance().getEventMain().showLoading(false);
            });

            // ====== НОРМАЛЬНО ПРОПИСАННЫЕ СЛУШАТЕЛИ ======

            client.on("list_user", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    // Сервер может прислать:
                    // - массив пользователей (после login)
                    // - одного пользователя (broadcast после register)
                    // Так что обрабатываем оба варианта.
                    List<Model_User_Account> users = new ArrayList<>();

                    try {
                        for (Object o : os) {
                            if (o == null) continue;
                            Model_User_Account u = new Model_User_Account(o);

                            // user может быть null, если событие пришло до setUser()
                            if (user == null || u.getUserID() != user.getUserID()) {
                                users.add(u);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing list_user: " + e.getMessage());
                        return;
                    }

                    SwingUtilities.invokeLater(() -> {
                        if (PublicEvent.getInstance().getEventMenuLeft() != null) {
                            PublicEvent.getInstance().getEventMenuLeft().newUser(users);
                        }
                    });
                }
            });

            client.on("user_status", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    if (os == null || os.length < 2) return;

                    int userID;
                    boolean status;

                    try {
                        userID = (Integer) os[0];
                        status = (Boolean) os[1];
                    } catch (Exception e) {
                        System.err.println("Error parsing user_status: " + e.getMessage());
                        return;
                    }

                    SwingUtilities.invokeLater(() -> {
                        if (PublicEvent.getInstance().getEventMenuLeft() == null) return;

                        if (status) {
                            PublicEvent.getInstance().getEventMenuLeft().userConnect(userID);
                        } else {
                            PublicEvent.getInstance().getEventMenuLeft().userDisconnect(userID);
                        }
                    });
                }
            });

            client.on("receive_ms", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    if (os == null || os.length == 0 || os[0] == null) return;

                    Model_Receive_Message message;
                    try {
                        message = new Model_Receive_Message(os[0]);
                    } catch (Exception e) {
                        System.err.println("Error parsing receive_ms: " + e.getMessage());
                        return;
                    }

                    SwingUtilities.invokeLater(() -> {
                        if (PublicEvent.getInstance().getEventChat() != null) {
                            PublicEvent.getInstance().getEventChat().receiveMessage(message);
                        }
                    });
                }
            });

            client.open();
        } catch (URISyntaxException e) {
            error(e);
        }
    }

//    // то что было
//    public void startServer() {
//        try {
//            client = IO.socket("http://" + IP + ":" + PORT_NUMBER);
//            client.on("list_user", new Emitter.Listener() {
//                @Override
//                public void call(Object... os) {
//                    //  list user
//                    List<Model_User_Account> users = new ArrayList<>();
//                    for (Object o : os) {
//                        Model_User_Account u = new Model_User_Account(o);
//                        if (u.getUserID() != user.getUserID()) {
//                            users.add(u);
//                        }
//                    }
//                    PublicEvent.getInstance().getEventMenuLeft().newUser(users);
//                }
//            });
//            client.on("user_status", new Emitter.Listener() {
//                @Override
//                public void call(Object... os) {
//                    int userID = (Integer) os[0];
//                    boolean status = (Boolean) os[1];
//                    if (status) {
//                        //  connect
//                        PublicEvent.getInstance().getEventMenuLeft().userConnect(userID);
//                    } else {
//                        //  disconnect
//                        PublicEvent.getInstance().getEventMenuLeft().userDisconnect(userID);
//                    }
//                }
//            });
//            client.on("receive_ms", new Emitter.Listener() {
//                @Override
//                public void call(Object... os) {
//                    Model_Receive_Message message = new Model_Receive_Message(os[0]);
//                    PublicEvent.getInstance().getEventChat().receiveMessage(message);
//                }
//            });
//            client.open();
//        } catch (URISyntaxException e) {
//            error(e);
//        }
//    }

    public Model_File_Sender addFile(File file, Model_Send_Message message) throws IOException {
        Model_File_Sender data = new Model_File_Sender(file, client, message);
        message.setFile(data);
        fileSender.add(data);
        //  For send file one by one
        if (fileSender.size() == 1) {
            data.initSend();
        }
        return data;
    }

    public void fileSendFinish(Model_File_Sender data) throws IOException {
        fileSender.remove(data);
        if (!fileSender.isEmpty()) {
            //  Start send new file when old file sending finish
            fileSender.get(0).initSend();
        }
    }

    public void fileReceiveFinish(Model_File_Receiver data) throws IOException {
        fileReceiver.remove(data);
        if (!fileReceiver.isEmpty()) {
            fileReceiver.get(0).initReceive();
        }
    }

    public void addFileReceiver(int fileID, EventFileReceiver event) throws IOException {
        Model_File_Receiver data = new Model_File_Receiver(fileID, client, event);
        fileReceiver.add(data);
        if (fileReceiver.size() == 1) {
            data.initReceive();
        }
    }

    public Socket getClient() {
        return client;
    }

    public Model_User_Account getUser() {
        return user;
    }

    public void setUser(Model_User_Account user) {
        this.user = user;
    }

    private void error(Exception e) {
        System.err.println(e);
    }
}
