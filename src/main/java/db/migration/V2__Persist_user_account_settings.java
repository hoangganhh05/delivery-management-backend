package db.migration;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

/**
 * Adds persisted profile, address-book, and preference storage to the existing
 * MySQL schema. Databases created before Flyway are baselined at V1.
 *
 * <p>No legacy contact value is rewritten or deleted. A normalized preflight
 * aborts with counts when blank or duplicate email/phone data needs review.</p>
 */
public class V2__Persist_user_account_settings extends BaseJavaMigration {

    private static final String USERS = "users";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!isMySql(connection)) {
            // H2 tests use Hibernate's disposable create-drop schema.
            return;
        }
        if (!tableExists(connection, USERS)) {
            throw new FlywayException(
                    "Flyway V2 requires the existing users table; baseline the core schema first"
            );
        }

        preflightLegacyUsers(connection);
        migrateUsers(connection);
        createUserAddresses(connection);
        createAndBackfillUserSettings(connection);
    }

    private void preflightLegacyUsers(Connection connection) throws SQLException {
        requireColumns(connection, USERS, "id", "password", "email", "phone_number");

        long blankEmails = count(connection,
                "SELECT COUNT(*) FROM `users` WHERE `email` IS NOT NULL AND TRIM(`email`) = ''");
        long blankPhones = count(connection,
                "SELECT COUNT(*) FROM `users` WHERE `phone_number` IS NOT NULL "
                        + "AND TRIM(`phone_number`) = ''");
        long duplicateEmails = count(connection, """
                SELECT COUNT(*) FROM (
                    SELECT LOWER(TRIM(`email`))
                    FROM `users`
                    WHERE `email` IS NOT NULL AND TRIM(`email`) <> ''
                    GROUP BY LOWER(TRIM(`email`))
                    HAVING COUNT(*) > 1
                ) `duplicate_email_groups`
                """);
        long duplicatePhones = count(connection, """
                SELECT COUNT(*) FROM (
                    SELECT TRIM(`phone_number`)
                    FROM `users`
                    WHERE `phone_number` IS NOT NULL AND TRIM(`phone_number`) <> ''
                    GROUP BY TRIM(`phone_number`)
                    HAVING COUNT(*) > 1
                ) `duplicate_phone_groups`
                """);
        long nullPasswords = count(connection,
                "SELECT COUNT(*) FROM `users` WHERE `password` IS NULL");
        long oversizedPasswords = count(connection,
                "SELECT COUNT(*) FROM `users` WHERE CHAR_LENGTH(`password`) > 255");

        if (blankEmails > 0 || blankPhones > 0 || duplicateEmails > 0
                || duplicatePhones > 0 || nullPasswords > 0 || oversizedPasswords > 0) {
            throw new FlywayException(
                    "Account-settings preflight failed; no legacy data was changed. "
                            + "Manual cleanup required: blankEmailRows=" + blankEmails
                            + ", blankPhoneRows=" + blankPhones
                            + ", duplicateEmailGroups=" + duplicateEmails
                            + ", duplicatePhoneGroups=" + duplicatePhones
                            + ", nullPasswordRows=" + nullPasswords
                            + ", passwordRowsOver255Chars=" + oversizedPasswords
            );
        }
    }

    private void migrateUsers(Connection connection) throws SQLException {
        ensureColumn(connection, USERS, "date_of_birth", "DATE NULL");
        ensureColumn(connection, USERS, "gender", "VARCHAR(20) NULL");
        ensureColumn(connection, USERS, "avatar_url", "VARCHAR(1024) NULL");
        ensureColumn(
                connection,
                USERS,
                "updated_at",
                "DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
        );
        ensureColumn(connection, USERS, "password_changed_at", "DATETIME NULL");

        // The preflight guarantees that narrowing an oversized legacy column is safe.
        execute(connection, "ALTER TABLE `users` MODIFY COLUMN `password` VARCHAR(255) NOT NULL");
        ensureSingleColumnUniqueIndex(connection, USERS, "email", "uk_users_email");
        ensureSingleColumnUniqueIndex(connection, USERS, "phone_number", "uk_users_phone_number");
    }

    private void createUserAddresses(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS `user_addresses` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `user_id` BIGINT NOT NULL,
                    `label` VARCHAR(50) NOT NULL,
                    `recipient_name` VARCHAR(100) NOT NULL,
                    `phone_number` VARCHAR(20) NOT NULL,
                    `address_line` VARCHAR(255) NOT NULL,
                    `ward` VARCHAR(100) NULL,
                    `district` VARCHAR(100) NULL,
                    `province` VARCHAR(100) NULL,
                    `postal_code` VARCHAR(20) NULL,
                    `is_default` TINYINT(1) NOT NULL DEFAULT 0,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    KEY `idx_user_addresses_user_id` (`user_id`),
                    CONSTRAINT `fk_user_addresses_user`
                        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        requireColumns(
                connection,
                "user_addresses",
                "id", "user_id", "label", "recipient_name", "phone_number",
                "address_line", "ward", "district", "province", "postal_code",
                "is_default", "created_at", "updated_at"
        );
        ensureIndex(connection, "user_addresses", "user_id", "idx_user_addresses_user_id");
    }

    private void createAndBackfillUserSettings(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS `user_settings` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `user_id` BIGINT NOT NULL,
                    `email_notifications` TINYINT(1) NOT NULL DEFAULT 1,
                    `sms_notifications` TINYINT(1) NOT NULL DEFAULT 0,
                    `push_notifications` TINYINT(1) NOT NULL DEFAULT 1,
                    `new_order_notifications` TINYINT(1) NOT NULL DEFAULT 1,
                    `status_change_notifications` TINYINT(1) NOT NULL DEFAULT 1,
                    `payment_success_notifications` TINYINT(1) NOT NULL DEFAULT 1,
                    `delivery_complete_notifications` TINYINT(1) NOT NULL DEFAULT 1,
                    `shipper_assignment_notifications` TINYINT(1) NOT NULL DEFAULT 0,
                    `service_alert_notifications` TINYINT(1) NOT NULL DEFAULT 1,
                    `language` VARCHAR(10) NOT NULL DEFAULT 'vi',
                    `theme` VARCHAR(20) NOT NULL DEFAULT 'LIGHT',
                    `accent_color` VARCHAR(7) NOT NULL DEFAULT '#2563EB',
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_user_settings_user_id` (`user_id`),
                    CONSTRAINT `fk_user_settings_user`
                        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        requireColumns(
                connection,
                "user_settings",
                "id", "user_id", "email_notifications", "sms_notifications",
                "push_notifications", "new_order_notifications",
                "status_change_notifications", "payment_success_notifications",
                "delivery_complete_notifications", "shipper_assignment_notifications",
                "service_alert_notifications", "language", "theme", "accent_color",
                "created_at", "updated_at"
        );
        assertNoDuplicateOwner(connection, "user_settings", "user_id");
        ensureSingleColumnUniqueIndex(
                connection,
                "user_settings",
                "user_id",
                "uk_user_settings_user_id"
        );

        execute(connection, """
                INSERT INTO `user_settings` (
                    `user_id`, `email_notifications`, `sms_notifications`,
                    `push_notifications`, `new_order_notifications`,
                    `status_change_notifications`, `payment_success_notifications`,
                    `delivery_complete_notifications`, `shipper_assignment_notifications`,
                    `service_alert_notifications`, `language`, `theme`, `accent_color`,
                    `created_at`, `updated_at`
                )
                SELECT
                    `u`.`id`, 1, 0, 1, 1, 1, 1, 1, 0, 1,
                    'vi', 'LIGHT', '#2563EB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                FROM `users` `u`
                LEFT JOIN `user_settings` `s` ON `s`.`user_id` = `u`.`id`
                WHERE `s`.`id` IS NULL
                """);
    }

    private void assertNoDuplicateOwner(Connection connection, String table, String ownerColumn)
            throws SQLException {
        long duplicateGroups = count(
                connection,
                "SELECT COUNT(*) FROM (SELECT `" + ownerColumn + "` FROM `" + table + "` "
                        + "GROUP BY `" + ownerColumn + "` HAVING COUNT(*) > 1) `duplicates`"
        );
        if (duplicateGroups > 0) {
            throw new FlywayException(
                    "Cannot enforce one-to-one " + table + "." + ownerColumn
                            + ": duplicateGroups=" + duplicateGroups + "; no rows were deleted"
            );
        }
    }

    private void requireColumns(Connection connection, String table, String... columns)
            throws SQLException {
        for (String column : columns) {
            if (!columnExists(connection, table, column)) {
                throw new FlywayException(
                        "Existing table " + table + " is missing required column " + column
                                + "; manual schema review is required"
                );
            }
        }
    }

    private void ensureColumn(
            Connection connection,
            String table,
            String column,
            String definition
    ) throws SQLException {
        if (!columnExists(connection, table, column)) {
            execute(
                    connection,
                    "ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition
            );
        }
    }

    private void ensureSingleColumnUniqueIndex(
            Connection connection,
            String table,
            String column,
            String preferredName
    ) throws SQLException {
        if (!hasSingleColumnUniqueIndex(connection, table, column)) {
            String indexName = availableIndexName(connection, table, preferredName);
            execute(
                    connection,
                    "CREATE UNIQUE INDEX `" + indexName + "` ON `" + table + "` (`" + column + "`)"
            );
        }
    }

    private void ensureIndex(Connection connection, String table, String column, String preferredName)
            throws SQLException {
        if (!hasLeadingIndexColumn(connection, table, column)) {
            String indexName = availableIndexName(connection, table, preferredName);
            execute(
                    connection,
                    "CREATE INDEX `" + indexName + "` ON `" + table + "` (`" + column + "`)"
            );
        }
    }

    private String availableIndexName(Connection connection, String table, String preferredName)
            throws SQLException {
        if (!indexExists(connection, table, preferredName)) {
            return preferredName;
        }
        int suffix = 2;
        while (indexExists(connection, table, preferredName + "_" + suffix)) {
            suffix++;
        }
        return preferredName + "_" + suffix;
    }

    private boolean hasSingleColumnUniqueIndex(Connection connection, String table, String column)
            throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM (
                    SELECT `INDEX_NAME`
                    FROM `information_schema`.`STATISTICS`
                    WHERE `TABLE_SCHEMA` = DATABASE()
                      AND `TABLE_NAME` = ?
                      AND `NON_UNIQUE` = 0
                    GROUP BY `INDEX_NAME`
                    HAVING COUNT(*) = 1
                       AND MAX(CASE WHEN `COLUMN_NAME` = ? THEN 1 ELSE 0 END) = 1
                ) `single_column_unique_indexes`
                """;
        return metadataObjectExists(connection, sql, table, column);
    }

    private boolean hasLeadingIndexColumn(Connection connection, String table, String column)
            throws SQLException {
        return metadataObjectExists(
                connection,
                "SELECT COUNT(*) FROM `information_schema`.`STATISTICS` "
                        + "WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = ? "
                        + "AND `COLUMN_NAME` = ? AND `SEQ_IN_INDEX` = 1",
                table,
                column
        );
    }

    private boolean indexExists(Connection connection, String table, String indexName) throws SQLException {
        return metadataObjectExists(
                connection,
                "SELECT COUNT(*) FROM `information_schema`.`STATISTICS` "
                        + "WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = ? AND `INDEX_NAME` = ?",
                table,
                indexName
        );
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        return metadataObjectExists(
                connection,
                "SELECT COUNT(*) FROM `information_schema`.`TABLES` "
                        + "WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = ?",
                table,
                null
        );
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        return metadataObjectExists(
                connection,
                "SELECT COUNT(*) FROM `information_schema`.`COLUMNS` "
                        + "WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = ? AND `COLUMN_NAME` = ?",
                table,
                column
        );
    }

    private boolean metadataObjectExists(
            Connection connection,
            String sql,
            String firstValue,
            String secondValue
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firstValue);
            if (secondValue != null) {
                statement.setString(2, secondValue);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private long count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private boolean isMySql(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase(Locale.ROOT).contains("mysql");
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
