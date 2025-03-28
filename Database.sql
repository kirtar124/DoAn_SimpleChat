CREATE DATABASE SimpleMessagingAppDB;
GO

-- Sử dụng cơ sở dữ liệu
USE SimpleMessagingAppDB;
GO

-- Tạo bảng Users (Lưu thông tin người dùng)
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    Password NVARCHAR(50) NOT NULL
);
GO

-- Tạo bảng Friends (Lưu quan hệ bạn bè)
CREATE TABLE Friends (
    FriendID int IDENTITY(1,1) NOT NULL,
    UserID1 INT NOT NULL,
    UserID2 INT NOT NULL,
    PRIMARY KEY (UserID1, UserID2),
    FOREIGN KEY (UserID1) REFERENCES Users(UserID),
    FOREIGN KEY (UserID2) REFERENCES Users(UserID),
    CHECK (UserID1 < UserID2) -- Đảm bảo UserID1 < UserID2 để tránh trùng lặp
);
GO

-- Tạo bảng Messages (Lưu tin nhắn)
CREATE TABLE Messages (
    MessageID INT IDENTITY(1,1) PRIMARY KEY,
    SenderID INT NOT NULL,
    ReceiverID INT NOT NULL,
    MessageText NVARCHAR(MAX) NOT NULL,
    SentTime DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (SenderID) REFERENCES Users(UserID),
    FOREIGN KEY (ReceiverID) REFERENCES Users(UserID)
);