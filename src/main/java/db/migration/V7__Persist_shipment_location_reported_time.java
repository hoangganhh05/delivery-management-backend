package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Retains the time attached to a GPS point by the shipper device. */
public class V7__Persist_shipment_location_reported_time extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String database = connection.getMetaData().getDatabaseProductName().toLowerCase();
        if (!database.contains("mysql") && !database.contains("mariadb")) return;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.COLUMNS "
                             + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' "
                             + "AND COLUMN_NAME = 'location_reported_at'")) {
            result.next();
            if (result.getInt(1) == 0) {
                try (Statement alter = connection.createStatement()) {
                    alter.execute("ALTER TABLE `shipments` ADD COLUMN `location_reported_at` DATETIME NULL");
                }
            }
        }
    }
}
