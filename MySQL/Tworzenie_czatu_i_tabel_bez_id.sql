CREATE SCHEMA IF NOT EXISTS `chat` DEFAULT CHARACTER SET utf8 COLLATE utf8_polish_ci ;
USE `chat` ;
create table `chat`.`users` (
	`name` VARCHAR(50) NOT NULL PRIMARY KEY,
    `password` VARCHAR(50),
    `log_date` DATETIME);
    
create TABLE `chat`.`messages`(
		`from` VARCHAR(50),
        `to` VARCHAR(50),
        `channelname` VARCHAR(50),
        `message` VARCHAR(500),
        `send_date` DATETIME,
        `odczytano` BOOLEAN);
        
create TABLE `chat`.`chatname` (
	`chatname` VARCHAR(50) NOT NULL PRIMARY KEY);
    
create TABLE `chat`.`userslistchat`(
	`username` VARCHAR(50),
    `chatname` VARCHAR(50),
    FOREIGN KEY (chatname) REFERENCES chatname (`chatname`));
    

        
        
        

     