package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Locale;

/** Creates the core schema for a brand-new MySQL/MariaDB installation. */
public class V1__Create_core_schema extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!isMySql(connection)) {
            // Tests use Hibernate create-drop with H2.
            return;
        }

        execute(connection, """
                CREATE TABLE IF NOT EXISTS `users` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `username` VARCHAR(50) NOT NULL,
                    `password` VARCHAR(255) NOT NULL,
                    `full_name` VARCHAR(100) NULL,
                    `email` VARCHAR(100) NULL,
                    `phone_number` VARCHAR(20) NULL,
                    `date_of_birth` DATE NULL,
                    `gender` VARCHAR(20) NULL,
                    `avatar_url` VARCHAR(1024) NULL,
                    `avatar_data` MEDIUMTEXT NULL,
                    `role` VARCHAR(20) NOT NULL,
                    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    `password_changed_at` DATETIME NULL,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_users_username` (`username`),
                    UNIQUE KEY `uk_users_email` (`email`),
                    UNIQUE KEY `uk_users_phone_number` (`phone_number`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS `vouchers` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `code` VARCHAR(50) NOT NULL,
                    `discount_percent` INT NULL,
                    `max_discount_amount` DECIMAL(12,2) NULL,
                    `min_order_amount` DECIMAL(12,2) NULL,
                    `start_date` DATETIME NULL,
                    `end_date` DATETIME NULL,
                    `usage_limit` INT NULL,
                    `active` TINYINT(1) NOT NULL DEFAULT 1,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_vouchers_code` (`code`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS `orders` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `tracking_number` VARCHAR(50) NOT NULL,
                    `sender_id` BIGINT NULL,
                    `voucher_id` BIGINT NULL,
                    `sender_name` VARCHAR(100) NULL,
                    `sender_phone` VARCHAR(20) NULL,
                    `sender_address` TEXT NULL,
                    `receiver_name` VARCHAR(100) NULL,
                    `receiver_phone` VARCHAR(20) NULL,
                    `receiver_address` TEXT NULL,
                    `weight_gram` INT NULL,
                    `shipping_fee` DECIMAL(12,2) NULL,
                    `service_type` VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
                    `discount_fee` DECIMAL(12,2) NULL,
                    `total_fee` DECIMAL(12,2) NULL,
                    `total_price` DECIMAL(15,2) NULL,
                    `cod_amount` DECIMAL(12,2) NULL,
                    `payment_method` VARCHAR(20) NOT NULL DEFAULT 'COD',
                    `payment_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    `paid_at` DATETIME NULL,
                    `payment_reference` VARCHAR(100) NULL,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `status` VARCHAR(30) NULL,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_orders_tracking_number` (`tracking_number`),
                    KEY `idx_orders_sender_id` (`sender_id`),
                    KEY `idx_orders_created_at` (`created_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS `order_items` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `order_id` BIGINT NOT NULL,
                    `item_name` VARCHAR(255) NOT NULL,
                    `quantity` INT NOT NULL,
                    `price` DECIMAL(12,2) NULL,
                    `weight_gram` INT NULL,
                    `declared_value` DECIMAL(12,2) NULL,
                    PRIMARY KEY (`id`),
                    KEY `idx_order_items_order_id` (`order_id`),
                    CONSTRAINT `fk_order_items_order`
                        FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS `shipments` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `order_id` BIGINT NOT NULL,
                    `shipper_id` BIGINT NULL,
                    `status` VARCHAR(30) NOT NULL,
                    `note` TEXT NULL,
                    `proof_image_url` VARCHAR(500) NULL,
                    `current_latitude` DECIMAL(10,7) NULL,
                    `current_longitude` DECIMAL(10,7) NULL,
                    `current_accuracy_m` DECIMAL(10,2) NULL,
                    `location_updated_at` DATETIME NULL,
                    `location_reported_at` DATETIME NULL,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    KEY `idx_shipments_order_id` (`order_id`),
                    KEY `idx_shipments_shipper_id` (`shipper_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS `notifications` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `user_id` BIGINT NOT NULL,
                    `title` VARCHAR(200) NOT NULL,
                    `message` TEXT NOT NULL,
                    `type` VARCHAR(50) NULL,
                    `reference_id` BIGINT NULL,
                    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    KEY `idx_notifications_user_read` (`user_id`, `is_read`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
    }

    private boolean isMySql(Connection connection) throws Exception {
        String product = connection.getMetaData().getDatabaseProductName();
        if (product == null) return false;
        String normalized = product.toLowerCase(Locale.ROOT);
        return normalized.contains("mysql") || normalized.contains("mariadb");
    }

    private void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
