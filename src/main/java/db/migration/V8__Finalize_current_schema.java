package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

/** Moves the last runtime schema repairs into a repeatable, validated migration step. */
public class V8__Finalize_current_schema extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!isMySql(connection)) return;

        addColumn(connection, "orders", "total_price", "DECIMAL(15,2) NULL");
        addColumn(connection, "order_items", "price", "DECIMAL(12,2) NULL");
        addColumn(connection, "order_items", "weight_gram", "INT NULL");
        addColumn(connection, "order_items", "declared_value", "DECIMAL(12,2) NULL");
        addColumn(connection, "vouchers", "active", "TINYINT(1) NOT NULL DEFAULT 1");

        addIndex(connection, "orders", "idx_orders_sender_id", "`sender_id`");
        addIndex(connection, "orders", "idx_orders_created_at", "`created_at`");
        addIndex(connection, "order_items", "idx_order_items_order_id", "`order_id`");
        addIndex(connection, "shipments", "idx_shipments_order_id", "`order_id`");
        addIndex(connection, "shipments", "idx_shipments_shipper_id", "`shipper_id`");
        addIndex(connection, "notifications", "idx_notifications_user_read", "`user_id`, `is_read`");
    }

    private void addColumn(Connection connection, String table, String column, String definition)
            throws Exception {
        if (!columnExists(connection, table, column)) {
            execute(connection, "ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
        }
    }

    private void addIndex(Connection connection, String table, String index, String columns)
            throws Exception {
        if (!indexExists(connection, table, index)) {
            execute(connection, "CREATE INDEX `" + index + "` ON `" + table + "` (" + columns + ")");
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws Exception {
        return exists(connection,
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                table, column);
    }

    private boolean indexExists(Connection connection, String table, String index) throws Exception {
        return exists(connection,
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                table, index);
    }

    private boolean exists(Connection connection, String sql, String first, String second) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, first);
            statement.setString(2, second);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
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
