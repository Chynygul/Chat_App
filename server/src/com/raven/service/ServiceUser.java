package com.raven.service;

import com.raven.connection.DatabaseConnection;
import com.raven.model.Model_Client;
import com.raven.model.Model_Login;
import com.raven.model.Model_Message;
import com.raven.model.Model_Register;
import com.raven.model.Model_User_Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser {

    public ServiceUser() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }

    public Model_Message register(Model_Register data) {
        //  Check user exit
        Model_Message message = new Model_Message();
        try {
            PreparedStatement p = con.prepareStatement(CHECK_USER);
            p.setString(1, data.getEmail());
            ResultSet r = p.executeQuery();
            if (r.next()) {
                message.setAction(false);
                message.setMessage("Email already registered");
            } else {
                message.setAction(true);
            }
            r.close();
            p.close();
            if (message.isAction()) {
                //  Insert User Register
                con.setAutoCommit(false);
                p = con.prepareStatement(INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS);
                p.setString(1, data.getUserName());
                p.setString(2, data.getPassword());
                p.setString(3, data.getEmail());
                p.execute();
                r = p.getGeneratedKeys();
                r.next();
                int userID = r.getInt(1);
                r.close();
                p.close();
                //  Create user account
                p = con.prepareStatement(INSERT_USER_ACCOUNT);
                p.setInt(1, userID);
                p.setString(2, data.getUserName());
                p.setString(3, data.getEmail());  // ✅ добавь это!
                p.execute();
                p.close();
                con.commit();
                con.setAutoCommit(true);
                message.setAction(true);
                message.setMessage("Ok");
                message.setData(new Model_User_Account(userID, data.getUserName(), data.getEmail(), "", "", true));
            }
        } catch (SQLException e) {
            message.setAction(false);
            message.setMessage("Server Error: " + e.getMessage());  // добавлено сообщение ошибки
            e.printStackTrace();  // покажет ошибку в консоли
            try {
                if (con.getAutoCommit() == false) {
                    con.rollback();
                    con.setAutoCommit(true);
                }
            } catch (SQLException e1) {
                e1.printStackTrace();  // и это тоже покажет
            }
        }
        return message;
    }

    public Model_Message loginWithReason(Model_Login login) throws SQLException {

        // 1) есть ли такой username (и активен ли)
        String sqlUser =
                "SELECT u.userid, u.password, u.email, ua.username, ua.gender, ua.imagestring " +
                        "FROM users u JOIN user_account ua USING (userid) " +
                        "WHERE ua.status='1' AND u.username=?";

        try (PreparedStatement p = con.prepareStatement(sqlUser)) {
            p.setString(1, login.getUserName());

            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    return new Model_Message(false, "USER_NOT_FOUND", null);
                }

                String emailFromDB = r.getString("email");
                if (!login.getEmail().equalsIgnoreCase(emailFromDB)) {
                    return new Model_Message(false, "EMAIL_NOT_MATCH", null);
                }

                String passFromDB = r.getString("password");
                if (!login.getPassword().equals(passFromDB)) {
                    return new Model_Message(false, "WRONG_PASSWORD", null);
                }

                Model_User_Account user = new Model_User_Account(
                        r.getInt("userid"),
                        r.getString("username"),
                        emailFromDB,
                        r.getString("gender"),
                        r.getString("imagestring"),
                        true
                );

                return new Model_Message(true, "OK", user);
            }
        }
    }


// изначальный код
//    public List<Model_User_Account> getUser(int exitUser) throws SQLException {
//        List<Model_User_Account> list = new ArrayList<>();
//        PreparedStatement p = con.prepareStatement(SELECT_USER_ACCOUNT);
//        p.setInt(1, exitUser);
//        ResultSet r = p.executeQuery();
//        while (r.next()) {
//            int userID = r.getInt(1);
//            String userName = r.getString(2);
//            String email = r.getString(5);
//            String gender = r.getString(3);
//            String image = r.getString(4);
//            list.add(new Model_User_Account(userID, userName, gender, image, email, checkUserStatus(userID)));
//        }
//        r.close();
//        p.close();
//        return list;
//    }

    // код от deepseek
    public List<Model_User_Account> getUser(int excludeUser) throws SQLException {
        List<Model_User_Account> list = new ArrayList<>();

        try (PreparedStatement p = con.prepareStatement(SELECT_USER_ACCOUNT)) {
            p.setInt(1, excludeUser);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    int userID = r.getInt(1);
                    String userName = r.getString(2);
                    String email = r.getString(5);
                    String gender = r.getString(3) != null ? r.getString(3) : "";
                    String image = r.getString(4) != null ? r.getString(4) : "";

                    list.add(new Model_User_Account(
                            userID,
                            userName,
                            gender,
                            image,
                            email,
                            checkUserStatus(userID)
                    ));
                }
            }
        }
        return list;
    }

    private boolean checkUserStatus(int userID) {
        List<Model_Client> clients = Service.getInstance(null).getListClient();
        for (Model_Client c : clients) {
            if (c.getUser().getUserID() == userID) {
                return true;
            }
        }
        return false;
    }

    //  SQL
//    private final String LOGIN = "select UserID, user_account.UserName, Gender, ImageString from `user` join user_account using (UserID) where `user`.UserName=BINARY(?) and `user`.`Password`=BINARY(?) and user_account.`Status`='1'";
//    private final String SELECT_USER_ACCOUNT = "select UserID, UserName, Gender, ImageString from user_account where user_account.`Status`='1' and UserID<>?";
//    private final String INSERT_USER = "insert into user (UserName, `Password`) values (?,?)";
//    private final String INSERT_USER_ACCOUNT = "insert into user_account (UserID, UserName) values (?,?)";
//    private final String CHECK_USER = "select UserID from user where UserName =? limit 1";
//

    private final String LOGIN =
            "SELECT userid, user_account.username, gender, imagestring " +
                    "FROM users JOIN user_account USING (userid) " +
                    "WHERE users.username = ? AND users.password = ? AND user_account.status = '1'";

    // Получить список всех пользователей, кроме текущего
    private final String SELECT_USER_ACCOUNT =
            "SELECT userid, username, gender, imagestring, email " +
                    "FROM user_account " +
                    "WHERE status = '1' AND userid <> ?";

    // Вставка нового пользователя в таблицу users
    private final String INSERT_USER =
            "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

    // Создание записи в user_account после регистрации
    private final String INSERT_USER_ACCOUNT =
            "INSERT INTO user_account (userid, username, email) VALUES (?, ?, ?)";

    // Проверка, существует ли уже пользователь с таким именем
    private final String CHECK_USER =
            "SELECT userid FROM users WHERE email = ? LIMIT 1";

// Instance
    private final Connection con;
}
