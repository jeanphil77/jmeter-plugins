package kg.apc.jmeter.dbmon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *
 * @author Marten Bohlin
 */
public class DbMonSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private String metricName;
    private String sql;
    private final Connection connection;
    private boolean sampleDeltaValue = true;
    private double oldValue = Double.NaN;

    public DbMonSampler(Connection conn, String name, boolean sampleDeltaValue, String sql) {
        this.metricName = name;
        this.connection = conn;
        this.sql = sql;
        this.sampleDeltaValue = sampleDeltaValue;
    }

    public void generateSamples(DbMonSampleGenerator collector) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
        
            if (rs.next()) {
                final double val = rs.getDouble(1);
                if (sampleDeltaValue) {
                    if(!Double.isNaN(oldValue)) {
                        collector.generateSample(val - oldValue, metricName);
                    }
                    oldValue = val;
                } else {
                    collector.generateSample(val, metricName);
                }
                
            }
        } catch (SQLException ex) {
            log.error("Error executing query: " + sql, ex);
        } finally {
            close(rs);
            close(stmt);
        }
    }

    private void close(ResultSet r) {
        if (r != null) {
            try {
                r.close();
            } catch (SQLException ex) {
                log.warn("Failed to close resultset", ex);
            }
        }
    }

    private void close(Statement r) {
        if (r != null) {
            try {
                r.close();
            } catch (SQLException ex) {
                log.warn("Failed to close statement", ex);
            }
        }
    }
}
