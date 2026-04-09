package dk.dbc.batchexchange;

import dk.dbc.commons.testcontainers.postgres.DBCPostgreSQLContainer;
import org.junit.After;
import org.junit.BeforeClass;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class IntegrationTest {
    public static final DBCPostgreSQLContainer dbcPostgreSQLContainer = makePostgresContainer();

    private static DBCPostgreSQLContainer makePostgresContainer() {
        final DBCPostgreSQLContainer postgreSQLContainer = new DBCPostgreSQLContainer();
        postgreSQLContainer.start();
        postgreSQLContainer.exposeHostPort();
        return postgreSQLContainer;
    }

    @BeforeClass
    public static void migrateDatabase() {
        final BatchExchangeDatabaseMigrator dbMigrator = new BatchExchangeDatabaseMigrator(
                dbcPostgreSQLContainer.datasource());
        dbMigrator.migrate();
    }

    @After
    public void resetDatabase() throws SQLException {
        try (Connection conn = dbcPostgreSQLContainer.createConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM entry");
            statement.executeUpdate("DELETE FROM batch");
            statement.executeUpdate("ALTER SEQUENCE entry_id_seq RESTART");
            statement.executeUpdate("ALTER SEQUENCE batch_id_seq RESTART");
        }
    }

    protected int getNumberOfActiveBatchEntries() {
        int result = -1;
        try (Connection conn = dbcPostgreSQLContainer.createConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT count(id) FROM entry WHERE status = 'ACTIVE'")) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    protected int getNumberOfPendingBatchEntries() {
        int result = -1;
        try (Connection conn = dbcPostgreSQLContainer.createConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT count(id) FROM entry WHERE status = 'PENDING'")) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    public static void executeScript(File script) {
        try (Connection conn = dbcPostgreSQLContainer.createConnection();
             FileInputStream fstream = new FileInputStream(script);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(new DataInputStream(fstream), StandardCharsets.UTF_8))) {

            final StringBuilder scriptLines = new StringBuilder();

            // Read file line by line, and append to StringBuilder
            String line;
            while ((line = reader.readLine()) != null) {
                scriptLines.append(line);
                scriptLines.append("\n");
            }
            conn.prepareStatement(scriptLines.toString()).executeUpdate();
        } catch (SQLException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
