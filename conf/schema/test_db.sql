CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_0900_as_ci NOT NULL,
  `country` varchar(100) COLLATE utf8mb4_0900_as_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_0900_as_ci NOT NULL,
  `dob` date NOT NULL,
  `phone` varchar(100) COLLATE utf8mb4_0900_as_ci NOT NULL,
  `pass` varchar(100) COLLATE utf8mb4_0900_as_ci NOT NULL,
  `category` enum('Normal','Silver','Gold','Diamond') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL,
  `createdAt` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_un` (`user_id`),
  UNIQUE KEY `users_un1` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci

CREATE TABLE `sessions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `token_id` varchar(100) NOT NULL,
  `user_id` varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
  `token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `createdAt` timestamp NOT NULL,
  `expiresAt` timestamp NOT NULL,
  PRIMARY KEY (`id`,`token_id`),
  UNIQUE KEY `sessions_un` (`user_id`),
  UNIQUE KEY `sessions_un2` (`token_id`,`token`),
  CONSTRAINT `sessions_FK` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

INSERT INTO ruy5rj68j8xc1e8x.users
(user_id, email, country, name, dob, phone, pass, category, createdAt)
VALUES('3kNkfJCH4Pe@7j#!', 'barakatopeluwa91@gmail.com', 'Nigeria', 'Barakat Opeoluwa Adeagbo', '2004-01-10', '+234 123 456 7890', '$2a$10$u71p9OJI.vec5lr0DYUpiuTKrcJjFvl2S6IDyW7tvburK.X4JdmRy', 'Normal', '2021-11-28 23:49:39');