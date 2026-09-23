package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class V3__Add_payments_and_role_permissions extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql")) {
            return;
        }

        addColumn(connection, "orders", "payment_method", "VARCHAR(20) NOT NULL DEFAULT 'COD'");
        addColumn(connection, "orders", "payment_status", "VARCHAR(20) NOT NULL DEFAULT 'PENDING'");
        addColumn(connection, "orders", "paid_at", "DATETIME NULL");
        addColumn(connection, "orders", "payment_reference", "VARCHAR(100) NULL");

        execute(connection, "UPDATE orders SET payment_status = CASE "
                + "WHEN status IN ('PAID','DELIVERED','DONE','COMPLETED') THEN 'PAID' "
                + "WHEN status IN ('CANCELLED','FAILED') THEN 'FAILED' ELSE 'PENDING' END");

        execute(connection, "CREATE TABLE IF NOT EXISTS role_permissions ("
                + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "role VARCHAR(20) NOT NULL, permission_code VARCHAR(50) NOT NULL,"
                + "allowed TINYINT(1) NOT NULL DEFAULT 0, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "CONSTRAINT uk_role_permission UNIQUE (role, permission_code))");
    }

    private void addColumn(Connection connection, String table, String column, String definition) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM information_schema.COLUMNS "
                     + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table
                     + "' AND COLUMN_NAME = '" + column + "'")) {
            result.next();
            if (result.getInt(1) == 0) {
                execute(connection, "ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
            }
        }
    }

    private void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
