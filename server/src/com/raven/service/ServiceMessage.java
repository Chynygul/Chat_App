package com.raven.service;

import com.raven.model.Model_Receive_Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.raven.connection.DatabaseConnection;

public class ServiceMessage {

    private final Connection con;

    public ServiceMessage() {
        con = DatabaseConnection.getInstance().getConnection();
    }

    public void saveMessage(Model_Receive_Message msg, int toUserID) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message_type, message_text, file_name, file_size, file_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement p = con.prepareStatement(sql)) {
            p.setInt(1, msg.getFromUserID());
            p.setInt(2, toUserID);
            p.setInt(3, msg.getMessageType());
            p.setString(4, msg.getText());
            p.setString(5, msg.getFileName());
            p.setLong(6, msg.getFileSize());
            p.setInt(7, msg.getFileID());
            p.executeUpdate();
            System.out.println(msg.getText() + " has been saved successfully");
        }

    }

    public List<Model_Receive_Message> getChatHistory(int user1, int user2) throws SQLException {
        List<Model_Receive_Message> list = new ArrayList<>();

        String sql =
                "SELECT sender_id, receiver_id, message_type, message_text, file_name, file_size, file_id, sent_at " +
                        "FROM messages " +
                        "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                        "ORDER BY sent_at ASC";

        System.out.println(sql);

        try (PreparedStatement p = con.prepareStatement(sql)) {
            p.setInt(1, user1);
            p.setInt(2, user2);
            p.setInt(3, user2);
            p.setInt(4, user1);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Model_Receive_Message msg = new Model_Receive_Message();
                    msg.setFromUserID(r.getInt("sender_id"));
                    msg.setMessageType(r.getInt("message_type"));
                    msg.setText(r.getString("message_text"));
                    msg.setFileName(r.getString("file_name"));
                    msg.setFileSize(r.getLong("file_size"));
                    msg.setFileID(r.getInt("file_id"));
                    list.add(msg);
                }
            }
        }

        return list;
    }
}