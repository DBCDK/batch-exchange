/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange;

import dk.dbc.commons.jdbc.util.JDBCUtil;
import org.junit.After;
import org.junit.BeforeClass;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class IntegrationTest {
    protected static final PGSimpleDataSource datasource;

    static {
        datasource = new PGSimpleDataSource();
        datasource.setDatabaseName("batch_exchange");
        datasource.setServerName("localhost");
        datasource.setPortNumber(Integer.parseInt(System.getProperty("postgresql.port", "5432")));
        datasource.setUser(System.getProperty("user.name"));
        datasource.setPassword(System.getProperty("user.name"));
    }

    @BeforeClass
    public static void migrateDatabase() throws Exception {
        final BatchExchangeDatabaseMigrator dbMigrator = new BatchExchangeDatabaseMigrator(datasource);
        dbMigrator.migrate();
    }

    @After
    public void resetDatabase() throws SQLException {
        try (Connection conn = datasource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM entry");
            statement.executeUpdate("DELETE FROM batch");
            statement.executeUpdate("ALTER SEQUENCE entry_id_seq RESTART");
            statement.executeUpdate("ALTER SEQUENCE batch_id_seq RESTART");
        }
    }

    protected static void executeScript(File scriptFile) {
        try (Connection conn = datasource.getConnection()) {
            JDBCUtil.executeScript(conn, scriptFile, StandardCharsets.UTF_8.name());
        } catch (SQLException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected int getNumberOfActiveBatchEntries() {
        try (Connection conn = datasource.getConnection()) {
            return JDBCUtil.getFirstInt(conn, "SELECT count(id) FROM entry WHERE status = 'ACTIVE'");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    protected int getNumberOfPendingBatchEntries() {
        try (Connection conn = datasource.getConnection()) {
            return JDBCUtil.getFirstInt(conn, "SELECT count(id) FROM entry WHERE status = 'PENDING'");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
