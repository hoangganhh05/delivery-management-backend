package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Stores the latest GPS point for an assigned shipment. */
public class V6__Persist_shipment_location extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql")) {
            return;
        }
        addColumnIfMissing(connection, "current_latitude", "DECIMAL(10,7) NULL");
        addColumnIfMissing(connection, "current_longitude", "DECIMAL(10,7) NULL");
        addColumnIfMissing(connection, "current_accuracy_m", "DECIMAL(10,2) NULL");
        addColumnIfMissing(connection, "location_updated_at", "DATETIME NULL");
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.COLUMNS "
                             + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' "
                             + "AND COLUMN_NAME = '" + column + "'")) {
            result.next();
            if (result.getInt(1) == 0) {
                try (Statement alter = connection.createStatement()) {
                    alter.execute("ALTER TABLE `shipments` ADD COLUMN `" + column + "` " + definition);
                }
            }
        }
    }
}
