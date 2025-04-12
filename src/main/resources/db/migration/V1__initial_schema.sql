-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Telephone numbers table
CREATE TABLE telephone_numbers (
    id BIGSERIAL PRIMARY KEY,
    number VARCHAR(20) NOT NULL UNIQUE,
    country_code VARCHAR(5) NOT NULL,
    area_code VARCHAR(10),
    number_type VARCHAR(50),
    category VARCHAR(50),
    features TEXT,
    status VARCHAR(20) NOT NULL,
    batch_id VARCHAR(50),
    user_id BIGINT,
    reserved_until TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Number status history table
CREATE TABLE number_status_history (
    id BIGSERIAL PRIMARY KEY,
    telephone_number_id BIGINT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    user_id VARCHAR(50),
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_telephone_number FOREIGN KEY (telephone_number_id) REFERENCES telephone_numbers(id)
);

-- File uploads table
CREATE TABLE file_uploads (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    batch_id VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    total_records INTEGER,
    processed_records INTEGER,
    failed_records INTEGER,
    error_message TEXT,
    uploaded_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_telephone_numbers_status ON telephone_numbers(status);
CREATE INDEX idx_telephone_numbers_country_code ON telephone_numbers(country_code);
CREATE INDEX idx_telephone_numbers_area_code ON telephone_numbers(area_code);
CREATE INDEX idx_telephone_numbers_number_type ON telephone_numbers(number_type);
CREATE INDEX idx_telephone_numbers_category ON telephone_numbers(category);
CREATE INDEX idx_telephone_numbers_batch_id ON telephone_numbers(batch_id);
CREATE INDEX idx_telephone_numbers_user_id ON telephone_numbers(user_id);

CREATE INDEX idx_number_status_history_telephone_number_id ON number_status_history(telephone_number_id);
CREATE INDEX idx_number_status_history_created_at ON number_status_history(created_at);

CREATE INDEX idx_file_uploads_status ON file_uploads(status);
CREATE INDEX idx_file_uploads_uploaded_by ON file_uploads(uploaded_by);
