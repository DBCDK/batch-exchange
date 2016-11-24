/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange;

import dk.dbc.batchexchange.dto.BatchEntry;
import dk.dbc.batchexchange.dto.BatchEntryDiagnosticsConversion;
import dk.dbc.batchexchange.dto.BatchEntryStatusConversion;
import dk.dbc.batchexchange.dto.Diagnostic;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.util.PGobject;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BatchExchangeIT extends IntegrationTest {
    private final BatchExchange batchExchange = new BatchExchange();

    @Before
    public void populateDatabase() throws URISyntaxException {
        final URL resource = BatchExchangeIT.class.getResource("/populate.sql");
        executeScript(new File(resource.toURI()));
    }

    /**
     * When: claiming pending batch entries
     * Then: the claimed batch entries have their status set to ACTIVE
     *  And: the claimed batch entries are returned ordered by ID
     * When: resetting claimed batch entries
     * Then: then claimed batch entries have their status set back to PENDING
     * @throws SQLException on database error
     */
    @Test
    public void claimingBatchEntries() throws SQLException {
        List<BatchEntry> entries;
        try (Connection conn = datasource.getConnection()) {
            entries = batchExchange.claimBatchEntries(conn, 10);
        }
        assertThat("number of claimed batch entries", entries.size(), is(10));
        int id = 1;
        for (BatchEntry entry : entries) {
            assertThat("batch entry ID", entry.getId(), is(id++));
            assertThat("batch entry status", entry.getStatus(), is(BatchEntry.Status.ACTIVE));
        }
        assertThat("number of batch entries marked as ACTIVE", getNumberOfActiveBatchEntries(), is(10));

        try (Connection conn = datasource.getConnection()) {
            assertThat("number of reset batch entries", batchExchange.resetClaimedBatchEntries(conn), is(10));
        }
        assertThat("number of batch entries marked as ACTIVE after reset", getNumberOfActiveBatchEntries(), is(0));
        assertThat("number of batch entries marked as PENDING after reset", getNumberOfPendingBatchEntries(), is(30));
    }

    /**
     * When: claiming pending batch entries and none exist
     * Then: no batch entries are claimed
     * @throws SQLException on database error
     */
    @Test
    public void claimingPendingBatchEntries_whenNoneExist() throws SQLException {
        // First claim every batch entry
        try (Connection conn = datasource.getConnection()) {
            batchExchange.claimBatchEntries(conn, 1000);
        }

        // Then try to claim some more
        try (Connection conn = datasource.getConnection()) {
            List<BatchEntry> entries = batchExchange.claimBatchEntries(conn, 10);
            assertThat("number of claimed batch entries", entries.size(), is(0));
        }
    }

    /**
     * When: updating an existing batch entry
     * Then: update method returns true
     *  And: batch entry status is updated
     *  And: batch entry diagnostics are updated
     * @throws SQLException on database error
     */
    @Test
    public void update() throws SQLException {
        final BatchEntryStatusConversion batchEntryStatusConversion = new BatchEntryStatusConversion();
        final BatchEntryDiagnosticsConversion batchEntryDiagnosticsConversion = new BatchEntryDiagnosticsConversion();
        final BatchEntry entry = new BatchEntry(1, null, 1)
                .withStatus(BatchEntry.Status.OK)
                .withDiagnostics(Collections.singletonList(Diagnostic.createError("I failed")));

        try (Connection conn = datasource.getConnection()) {
            assertThat(batchExchange.updateBatchEntry(conn, entry), is(true));
        }

        try (Connection conn = datasource.getConnection();
             PreparedStatement lookup = conn.prepareStatement("SELECT status, diagnostics FROM entry WHERE id = ?")) {
            lookup.setInt(1, entry.getId());
            final ResultSet rs = lookup.executeQuery();
            rs.next();
            assertThat(batchEntryStatusConversion.toEntityAttribute(rs.getObject(1)), is(entry.getStatus()));
            assertThat(batchEntryDiagnosticsConversion.toEntityAttribute((PGobject) rs.getObject(2)), is(entry.getDiagnostics()));
        }
    }

    /**
     * When: updating a batch entry that does not exist
     * Then: update method returns false
     * @throws SQLException on database error
     */
    @Test
    public void updateBatchEntry_whenBatchEntryDoesNotExist_returnsFalse() throws SQLException {
        final BatchEntry entry = new BatchEntry(42000, null, 1)
                .withStatus(BatchEntry.Status.OK);
        try (Connection conn = datasource.getConnection()) {
            assertThat(batchExchange.updateBatchEntry(conn, entry), is(false));
        }
    }
}
