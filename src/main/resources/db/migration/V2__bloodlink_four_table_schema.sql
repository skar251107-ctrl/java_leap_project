DROP TABLE IF EXISTS donation_record;
DROP TABLE IF EXISTS donor;
DROP TABLE IF EXISTS blood_group;
DROP TABLE IF EXISTS admin_user;
DROP TABLE IF EXISTS donation_records;
DROP TABLE IF EXISTS donors;
DROP TABLE IF EXISTS blood_groups;
DROP TABLE IF EXISTS admin_users;
DROP TABLE IF EXISTS blood_requests;
DROP TABLE IF EXISTS recipients;
DROP TABLE IF EXISTS hospitals;
DROP TABLE IF EXISTS prescription;

CREATE TABLE blood_group (
    id BIGINT NOT NULL AUTO_INCREMENT,
    blood_group VARCHAR(5) NOT NULL,
    description VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_blood_group_value UNIQUE (blood_group)
);

CREATE TABLE donor (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    blood_group_id BIGINT NOT NULL,
    city VARCHAR(100) NOT NULL,
    last_donation_date DATE NULL,
    available BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_donor_blood_group FOREIGN KEY (blood_group_id) REFERENCES blood_group (id)
);

CREATE TABLE donation_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    donor_id BIGINT NOT NULL,
    donation_date DATE NOT NULL,
    hospital_name VARCHAR(150) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_donation_donor FOREIGN KEY (donor_id) REFERENCES donor (id)
);

CREATE TABLE admin_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(150) NULL,
    role VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_admin_username UNIQUE (username),
    CONSTRAINT uk_admin_email UNIQUE (email)
);