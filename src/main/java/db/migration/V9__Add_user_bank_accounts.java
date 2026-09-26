package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Locale;

/** Adds encrypted, customer-owned payout account storage. */
public class V9__Add_user_bank_accounts extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!isMySql(connection)) {
            // Tests use Hibernate create-drop with H2.
            return;
        }

        execute(connection, """
                CREATE TABLE IF NOT EXISTS `user_bank_accounts` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                    `user_id` BIGINT NOT NULL,
                    `bank_code` VARCHAR(30) NULL,
                    `bank_name` VARCHAR(100) NOT NULL,
                    `account_holder_name` VARCHAR(100) NOT NULL,
                    `account_number_encrypted` VARCHAR(512) NOT NULL,
                    `account_number_last4` VARCHAR(4) NOT NULL,
                    `is_default` TINYINT(1) NOT NULL DEFAULT 0,
                    `verified` TINYINT(1) NOT NULL DEFAULT 0,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    KEY `idx_user_bank_accounts_user_id` (`user_id`),
                    CONSTRAINT `fk_user_bank_accounts_user`
                        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
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
