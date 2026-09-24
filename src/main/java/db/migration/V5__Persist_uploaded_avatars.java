package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Stores compact, validated account avatars in the same durable database as profiles. */
public class V5__Persist_uploaded_avatars extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql")) {
            return;
        }

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.COLUMNS "
                             + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' "
                             + "AND COLUMN_NAME = 'avatar_data'"
             )) {
            result.next();
            if (result.getInt(1) == 0) {
                try (Statement alter = connection.createStatement()) {
                    alter.execute("ALTER TABLE `users` ADD COLUMN `avatar_data` MEDIUMTEXT NULL");
                }
            }
        }
    }
}
