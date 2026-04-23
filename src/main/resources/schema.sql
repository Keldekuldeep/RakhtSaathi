-- RakhtSaathi Database Schema
-- Run this manually or let Hibernate auto-create with ddl-auto=update

CREATE DATABASE IF NOT EXISTS rakhtsaathi;
USE rakhtsaathi;

-- USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    user_type ENUM('NEEDY', 'DONOR', 'ADMIN') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
);

-- NEEDY TABLE
CREATE TABLE IF NOT EXISTS needy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    city VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    relation_to_patient VARCHAR(50) NOT NULL,
    request_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_city (city)
);

-- DONORS TABLE
CREATE TABLE IF NOT EXISTS donors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    blood_group ENUM('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE') NOT NULL,
    city VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    age INT NOT NULL,
    weight DOUBLE NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    total_donations INT DEFAULT 0,
    rating DOUBLE DEFAULT 0.0,
    last_donation_date DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_blood_group_city (blood_group, city),
    INDEX idx_available (is_available)
);

-- BLOOD REQUESTS TABLE
CREATE TABLE IF NOT EXISTS blood_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    needy_id BIGINT NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    blood_group ENUM('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE') NOT NULL,
    units_needed INT NOT NULL,
    urgency ENUM('IMMEDIATE','WITHIN_24H','SCHEDULED') NOT NULL,
    hospital VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    attendant_name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(15) NOT NULL,
    additional_notes TEXT,
    voice_message_url VARCHAR(500),
    has_voice_message BOOLEAN DEFAULT FALSE,
    status ENUM('ACTIVE','FULFILLED','CANCELLED','EXPIRED') DEFAULT 'ACTIVE',
    notified_donors_count INT DEFAULT 0,
    accepted_donors_count INT DEFAULT 0,
    rejected_donors_count INT DEFAULT 0,
    fulfilled_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (needy_id) REFERENCES needy(id),
    INDEX idx_status (status),
    INDEX idx_blood_group (blood_group),
    INDEX idx_city (city),
    INDEX idx_created_at (created_at)
);

-- DONOR NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS donor_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_request_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    status ENUM('NOTIFIED','ACCEPTED','REJECTED') DEFAULT 'NOTIFIED',
    notified_at DATETIME,
    responded_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_request_donor (blood_request_id, donor_id),
    FOREIGN KEY (blood_request_id) REFERENCES blood_requests(id),
    FOREIGN KEY (donor_id) REFERENCES donors(id),
    INDEX idx_donor_status (donor_id, status)
);

-- FEEDBACK TABLE
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_request_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (blood_request_id) REFERENCES blood_requests(id),
    FOREIGN KEY (from_user_id) REFERENCES users(id),
    FOREIGN KEY (to_user_id) REFERENCES users(id)
);

-- CERTIFICATES TABLE
CREATE TABLE IF NOT EXISTS certificates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    blood_request_id BIGINT NOT NULL,
    certificate_url VARCHAR(500),
    certificate_number VARCHAR(100) NOT NULL UNIQUE,
    issued_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES donors(id),
    FOREIGN KEY (blood_request_id) REFERENCES blood_requests(id)
);

-- Default Admin User (password: admin123 - BCrypt encoded)
INSERT IGNORE INTO users (email, password, full_name, user_type, is_active)
VALUES ('admin@rakhtsaathi.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        'System Administrator', 'ADMIN', TRUE);
