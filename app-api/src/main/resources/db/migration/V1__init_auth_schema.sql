CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    gender VARCHAR(255),
    address TEXT,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_username ON refresh_tokens(username);

CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_password_reset_token_hash_used
    ON password_reset_token(token_hash, used);

INSERT INTO users (name, username, email, password, phone, gender, address, role)
SELECT
    'achraf Zaim',
    'achraf',
    'achraf@test.com',
    '$2a$10$mjOtFXdXsMxQcgbbU2pttuKj.dUtDS4eXsORYUfv0D4tuct3gZVkK',
    '+212600000000',
    'MALE',
    'El Jadida, Morocco',
    'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'achraf' OR email = 'achraf@test.com'
);
