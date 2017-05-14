CREATE SCHEMA IF NOT EXISTS `chat` DEFAULT CHARACTER SET utf8 COLLATE utf8_polish_ci ;
USE `chat` ;
create table `chat`.`users` (
	`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(50),
    `password` VARCHAR(50),
    `log_date` varchar(50))
    
create TABLE `chaT`.`messages`(
		`from` VARCHAR(50),
        `to` VARCHAR(50),
        `message` VARCHAR(500))

     