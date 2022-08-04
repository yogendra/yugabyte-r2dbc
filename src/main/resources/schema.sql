CREATE TABLE IF NOT EXISTS user_profile
(
  id         SERIAL PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name  VARCHAR(255) NOT NULL,
  login_id    VARCHAR(256) NOT NULL UNIQUE,
  password   VARCHAR(256) NOT NULL,
  enabled    BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS login_history
(
  id         SERIAL PRIMARY KEY,
  login_id    VARCHAR(256) NOT NULL,
  login_time TIMESTAMP,
  CONSTRAINT uk_login_history UNIQUE(login_id, login_time)
);
