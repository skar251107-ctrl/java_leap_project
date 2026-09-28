DELIMITER //
DROP PROCEDURE IF EXISTS bloodlink_four_table_migration//
CREATE PROCEDURE bloodlink_four_table_migration()
BEGIN
    DECLARE plural_donors INT DEFAULT 0;
    DECLARE singular_donors INT DEFAULT 0;
    DECLARE plural_donations INT DEFAULT 0;
    DECLARE singular_donations INT DEFAULT 0;
    DECLARE donor_group_column INT DEFAULT 0;
    DECLARE donor_group_id_column INT DEFAULT 0;
    DECLARE donor_fk INT DEFAULT 0;

    DROP TABLE IF EXISTS blood_requests;
    DROP TABLE IF EXISTS recipients;
    DROP TABLE IF EXISTS hospitals;

    CREATE TABLE IF NOT EXISTS blood_group (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        code VARCHAR(20) NOT NULL UNIQUE,
        label VARCHAR(5) NOT NULL UNIQUE
    );
    INSERT IGNORE INTO blood_group (code, label) VALUES
        ('A_POSITIVE', 'A+'), ('A_NEGATIVE', 'A-'),
        ('B_POSITIVE', 'B+'), ('B_NEGATIVE', 'B-'),
        ('AB_POSITIVE', 'AB+'), ('AB_NEGATIVE', 'AB-'),
        ('O_POSITIVE', 'O+'), ('O_NEGATIVE', 'O-');

    SELECT COUNT(*) INTO plural_donors FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'donors';
    SELECT COUNT(*) INTO singular_donors FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'donor';
    IF plural_donors > 0 AND singular_donors = 0 THEN
        RENAME TABLE donors TO donor;
        SET singular_donors = 1;
        SET plural_donors = 0;
    END IF;

    IF singular_donors > 0 THEN
        SELECT COUNT(*) INTO donor_group_column FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'donor' AND column_name = 'blood_group';
        SELECT COUNT(*) INTO donor_group_id_column FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'donor' AND column_name = 'blood_group_id';
        IF donor_group_id_column = 0 THEN
            ALTER TABLE donor ADD COLUMN blood_group_id BIGINT NULL;
        END IF;
        IF donor_group_column > 0 THEN
            UPDATE donor d JOIN blood_group g ON g.code = d.blood_group
                SET d.blood_group_id = g.id;
            ALTER TABLE donor DROP COLUMN blood_group;
        END IF;
    END IF;

    IF plural_donors > 0 AND singular_donors > 0 THEN
        INSERT IGNORE INTO donor (id, name, phone, city, last_donation_date, available, created_at, blood_group_id)
        SELECT d.id, d.name, d.phone, d.city, d.last_donation_date, d.available, d.created_at, g.id
        FROM donors d JOIN blood_group g ON g.code = d.blood_group;
    END IF;

    SELECT COUNT(*) INTO plural_donations FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'donation_records';
    SELECT COUNT(*) INTO singular_donations FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'donation_record';
    IF plural_donations > 0 AND singular_donations = 0 THEN
        RENAME TABLE donation_records TO donation_record;
        SET singular_donations = 1;
        SET plural_donations = 0;
    ELSEIF plural_donations > 0 AND singular_donations > 0 THEN
        INSERT IGNORE INTO donation_record (id, donation_date, hospital_name, donor_id, created_at)
        SELECT dr.id, dr.donation_date, dr.hospital_name, dr.donor_id, dr.created_at
        FROM donation_records dr JOIN donor d ON d.id = dr.donor_id;
    END IF;

    IF singular_donors > 0 THEN
        ALTER TABLE donor MODIFY COLUMN blood_group_id BIGINT NOT NULL;
        SELECT COUNT(*) INTO donor_fk FROM information_schema.key_column_usage
            WHERE table_schema = DATABASE() AND table_name = 'donor'
              AND column_name = 'blood_group_id' AND referenced_table_name = 'blood_group';
        IF donor_fk = 0 THEN
            ALTER TABLE donor ADD CONSTRAINT fk_donor_blood_group
                FOREIGN KEY (blood_group_id) REFERENCES blood_group (id);
        END IF;
    END IF;

    DROP TABLE IF EXISTS donation_records;
    DROP TABLE IF EXISTS donors;
END//
DELIMITER ;
CALL bloodlink_four_table_migration();
DROP PROCEDURE bloodlink_four_table_migration;