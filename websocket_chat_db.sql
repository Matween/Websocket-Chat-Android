/*
Created		16/09/2017
Modified		19/09/2017
Project		
Model		
Company		
Author		
Version		
Database		mySQL 5 
*/


Create table Users (
	UserID Int NOT NULL AUTO_INCREMENT,
	fullname Varchar(255),
	email Varchar(255),
	password Varchar(255),
	created Datetime,
	profilePic Varchar(255),
	UNIQUE (email),
	Index AI_UserID (UserID),
 Primary Key (UserID)
) ENGINE = MyISAM;

Create table UsersConversations (
	UserConversationID Int NOT NULL AUTO_INCREMENT,
	UserID Int NOT NULL,
	ConversationID Int NOT NULL,
	Index AI_UserConversationID (UserConversationID),
 Primary Key (UserConversationID),
 Constraint Relationship5 Foreign Key (UserID) references Users (UserID) on delete  restrict on update  restrict,
 Constraint Relationship6 Foreign Key (ConversationID) references Conversations (ConversationID) on delete  restrict on update  restrict
) ENGINE = MyISAM;

Create table Conversations (
	ConversationID Int NOT NULL AUTO_INCREMENT,
	created Datetime,
	Index AI_ConversationID (ConversationID),
 Primary Key (ConversationID)
) ENGINE = MyISAM;

Create table Messages (
	MessageID Int NOT NULL AUTO_INCREMENT,
	message Varchar(255),
	sent Datetime,
	UserID Int NOT NULL,
	ConversationID Int NOT NULL,
	Index AI_MessageID (MessageID),
 Primary Key (MessageID),
 Constraint Relationship7 Foreign Key (UserID) references Users (UserID) on delete  restrict on update  restrict,
 Constraint Relationship8 Foreign Key (ConversationID) references Conversations (ConversationID) on delete  restrict on update  restrict
) ENGINE = MyISAM;

Create table Friendships (
	FriendshipID Int NOT NULL AUTO_INCREMENT,
	FromUserID Int NOT NULL,
	ToUserID Int NOT NULL,
	started Datetime,
	ended Datetime DEFAULT NULL,
	Index AI_FriendshipID (FriendshipID),
 Primary Key (FriendshipID),
 Constraint Relationship9 Foreign Key (FromUserID) references Users (UserID) on delete  restrict on update  restrict,
 Constraint Relationship10 Foreign Key (ToUserID) references Users (UserID) on delete  restrict on update  restrict
) ENGINE = MyISAM;


