package com.example.doanchat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class connect {
    private final String classes = "net.sourceforge.jtds.jdbc.Driver";
    protected static String ip = "10.0.2.2";
    protected static String port = "1433";
    protected static String user = "kiem1";
    protected static String pass = "1";
    protected static String db = "SimpleMessagingAppDB";

    // Interface cho callback của CONN
    public interface ConnectionCallback {
        void onResult(Connection conn, String errorMessage);
    }

    // Phương thức kết nối không đồng bộ
    public void CONN(ConnectionCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Connection conn = null;
            String errorMessage = null;
            try {
                Class.forName(classes);
                System.out.println("Driver loaded successfully: " + classes);
                conn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + db, user, pass);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                System.out.println("Failed to load driver or connect: " + errorMessage);
            }

            final Connection finalConn = conn;
            final String finalErrorMessage = errorMessage;
            callback.onResult(finalConn, finalErrorMessage);
        });
    }

    // Phương thức thêm tài khoản mới
    public boolean addUser(String username, String password) {
        Connection conn = null;
        try {
            conn = getConnectionSync();
            String query = "INSERT INTO Users (Username, Password) VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            int rowsInserted = preparedStatement.executeUpdate();
            preparedStatement.close();
            closeConnection(conn);
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            closeConnection(conn);
            return false;
        }
    }

    // Phương thức kiểm tra tài khoản và mật khẩu
    public void checkLogin(String username, String password, CheckLoginCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Connection conn = null;
            int userId = -1;
            String errorMessage = null;
            try {
                conn = getConnectionSync();
                String query = "SELECT UserID FROM Users WHERE Username = ? AND Password = ?";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    userId = resultSet.getInt("UserID");
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            } finally {
                closeConnection(conn);
            }
            final int finalUserId = userId;
            final String finalErrorMessage = errorMessage;
            callback.onResult(finalUserId != -1, finalUserId, finalErrorMessage);
        });
    }

    // Interface cho callback của checkLogin
    public interface CheckLoginCallback {
        void onResult(boolean success, int userId, String errorMessage);
    }

    // Phương thức lấy danh sách bạn bè
    public void getFriends(int userId, GetFriendsCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            List<User> friends = new ArrayList<>();
            Connection conn = null;
            String errorMessage = null;
            try {
                conn = getConnectionSync();
                String query = "SELECT u.UserID, u.Username " +
                        "FROM Users u " +
                        "INNER JOIN Friends f ON (u.UserID = f.UserID1 OR u.UserID = f.UserID2) " +
                        "WHERE (f.UserID1 = ? OR f.UserID2 = ?) AND u.UserID != ?";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setInt(1, userId);
                preparedStatement.setInt(2, userId);
                preparedStatement.setInt(3, userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    int friendId = resultSet.getInt("UserID");
                    String friendUsername = resultSet.getString("Username");
                    User friend = new User(friendId, friendUsername);
                    friends.add(friend);
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            } finally {
                closeConnection(conn);
            }
            if (errorMessage != null) {
                friends = new ArrayList<>(); // Trả về danh sách rỗng nếu có lỗi
            }
            callback.onResult(friends);
        });
    }

    // Interface cho callback của getFriends
    public interface GetFriendsCallback {
        void onResult(List<User> friends);
    }

    // Phương thức tìm kiếm người dùng chưa là bạn bè
    public void searchUsersNotFriends(int currentUserId, String keyword, SearchUsersCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            List<User> users = new ArrayList<>();
            Connection conn = null;
            String errorMessage = null;
            try {
                conn = getConnectionSync();
                String query = "SELECT UserID, Username FROM Users " +
                        "WHERE Username LIKE '%' + ? + '%' " +
                        "AND UserID != ? " +
                        "AND UserID NOT IN (" +
                        "SELECT UserID1 FROM Friends WHERE UserID2 = ? " +
                        "UNION " +
                        "SELECT UserID2 FROM Friends WHERE UserID1 = ?)";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setString(1, keyword);
                preparedStatement.setInt(2, currentUserId);
                preparedStatement.setInt(3, currentUserId);
                preparedStatement.setInt(4, currentUserId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    int userId = resultSet.getInt("UserID");
                    String username = resultSet.getString("Username");
                    User user = new User(userId, username);
                    users.add(user);
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            } finally {
                closeConnection(conn);
            }
            if (errorMessage != null) {
                users = new ArrayList<>(); // Trả về danh sách rỗng nếu có lỗi
            }
            callback.onResult(users);
        });
    }

    // Interface cho callback của searchUsersNotFriends
    public interface SearchUsersCallback {
        void onResult(List<User> users);
    }

    // Phương thức thêm bạn bè
    public void addFriend(int userId1, int userId2, AddFriendCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Connection conn = null;
            boolean success = false;
            String errorMessage = null;
            try {
                conn = getConnectionSync();
                String query = "INSERT INTO Friends (UserID1, UserID2) VALUES (?, ?)";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setInt(1, userId1);
                preparedStatement.setInt(2, userId2);
                int rowsInserted = preparedStatement.executeUpdate();
                success = rowsInserted > 0;
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            } finally {
                closeConnection(conn);
            }
            callback.onResult(success, errorMessage);
        });
    }

    // Interface cho callback của addFriend
    public interface AddFriendCallback {
        void onResult(boolean success, String errorMessage);
    }

    // Phương thức gửi tin nhắn
    public void sendMessage(int senderId, int receiverId, String messageText, SendMessageCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Connection conn = null;
            boolean success = false;
            String errorMessage = null;
            try {
                conn = getConnectionSync();
                String query = "INSERT INTO Messages (SenderID, ReceiverID, MessageText, SentTime) VALUES (?, ?, ?, GETDATE())";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setInt(1, senderId);
                preparedStatement.setInt(2, receiverId);
                preparedStatement.setString(3, messageText);
                int rowsInserted = preparedStatement.executeUpdate();
                success = rowsInserted > 0;
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            } finally {
                closeConnection(conn);
            }
            callback.onResult(success, errorMessage);
        });
    }

    // Interface cho callback của sendMessage
    public interface SendMessageCallback {
        void onResult(boolean success, String errorMessage);
    }

    // Phương thức lấy tin nhắn
    public void getMessages(int userId, int friendId, GetMessagesCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            List<Message> messages = new ArrayList<>();
            Connection conn = null;
            String errorMessage = null;
            try {
                conn = getConnectionSync();
                String query = "SELECT * FROM Messages WHERE (SenderID = ? AND ReceiverID = ?) OR (SenderID = ? AND ReceiverID = ?) ORDER BY SentTime";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setInt(1, userId);
                preparedStatement.setInt(2, friendId);
                preparedStatement.setInt(3, friendId);
                preparedStatement.setInt(4, userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Message msg = new Message(
                            resultSet.getInt("MessageID"),
                            resultSet.getInt("SenderID"),
                            resultSet.getInt("ReceiverID"),
                            resultSet.getString("MessageText"),
                            resultSet.getTimestamp("SentTime")
                    );
                    messages.add(msg);
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            } finally {
                closeConnection(conn);
            }
            if (errorMessage != null) {
                messages = new ArrayList<>(); // Trả về danh sách rỗng nếu có lỗi
            }
            callback.onResult(messages);
        });
    }

    // Interface cho callback của getMessages
    public interface GetMessagesCallback {
        void onResult(List<Message> messages);
    }

    // Phương thức kết nối đồng bộ (chỉ dùng trong nội bộ class)
    private Connection getConnectionSync() throws SQLException {
        try {
            Class.forName(classes);
            System.out.println("Driver loaded successfully: " + classes);
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to load driver: " + e.getMessage());
            throw new SQLException("Driver not found: " + e.getMessage(), e);
        }
        return DriverManager.getConnection("jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + db, user, pass);
    }

    // Phương thức đóng kết nối
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}