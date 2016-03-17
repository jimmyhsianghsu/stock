DELETE FROM user_roles WHERE username='test';
DELETE FROM users WHERE username='test';
INSERT INTO users(username,password,enabled) VALUES ('test','$2a$10$j6OfqmYMZCmfu3k7v6yWMuQfyUaVD7T7FzdmGU6lT8jYBXoKnkfB.', TRUE);
INSERT INTO user_roles (username, ROLE) VALUES ('test', 'ROLE_STOCK');