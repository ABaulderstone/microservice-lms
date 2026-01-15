
CREATE DATABASE auth_db;
CREATE USER auth_user WITH PASSWORD 'auth_password';
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;


CREATE DATABASE profile_db;
CREATE USER profile_user WITH PASSWORD 'profile_password';
GRANT ALL PRIVILEGES ON DATABASE profile_db TO profile_user;
