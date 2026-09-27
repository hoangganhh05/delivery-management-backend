package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V10__Create_shipment_offers extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (var statement = context.getConnection().createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS shipment_offers (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, shipper_id BIGINT NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING', expires_at DATETIME NOT NULL, created_at DATETIME NOT NULL, responded_at DATETIME NULL, INDEX idx_offer_shipper_status (shipper_id,status), INDEX idx_offer_order_status (order_id,status))");
        }
    }
}
