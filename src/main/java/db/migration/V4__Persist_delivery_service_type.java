package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Persists the delivery service selected when an order is created. */
public class V4__Persist_delivery_service_type extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String database = connection.getMetaData().getDatabaseProductName().toLowerCase();
        if (!database.contains("mysql") && !database.contains("mariadb")) {
            return;
        }

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.COLUMNS "
                             + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' "
                             + "AND COLUMN_NAME = 'service_type'"
             )) {
            result.next();
            if (result.getInt(1) == 0) {
                try (Statement alter = connection.createStatement()) {
                    alter.execute("ALTER TABLE `orders` ADD COLUMN `service_type` "
                            + "VARCHAR(30) NOT NULL DEFAULT 'STANDARD'");
                }
            }
        }
    }
}
