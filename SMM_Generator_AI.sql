SELECT *FROM users;
SELECT *FROM roles;
SELECT *FROM ai_posts;
SELECT * FROM users_roles;

DROP TABLE users;
DROP TABLE ai_posts;
DROP TABLE roles;
DROP TABLE users_roles;

SELECT id, email FROM users WHERE email = 'admin@gmail.com';
SELECT id, name FROM roles WHERE name = 'ROLE_ADMIN';

SELECT u.email, r.name
FROM users u
JOIN users_roles ur ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id;