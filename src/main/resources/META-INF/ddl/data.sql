INSERT INTO users(username,password,enabled) VALUES ('mkyong','$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', TRUE);
INSERT INTO user_roles (username, ROLE) VALUES ('mkyong', 'ROLE_USER');
INSERT INTO user_roles (username, ROLE) VALUES ('mkyong', 'ROLE_ADMIN');
INSERT INTO user_roles (username, ROLE) VALUES ('mkyong', 'ROLE_DBM');
INSERT INTO user_roles (username, ROLE) VALUES ('mkyong', 'ROLE_STOCK');