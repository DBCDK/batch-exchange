/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange;

import dk.dbc.batchexchange.dto.BatchEntry;
import dk.dbc.batchexchange.dto.BatchEntryDiagnosticsConversion;
import dk.dbc.batchexchange.dto.BatchEntryMetadataConversion;
import dk.dbc.batchexchange.dto.BatchEntryStatusConversion;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the non-JPA API for the batch-exchange.
 * <P>
 * For JPA usage use the classes from the dk.dbc.batchexchange.dto directly.
 * </P>
 */
public class BatchExchange {
    private static final String UPDATE_BATCH_ENTRY_STATEMENT = "UPDATE entry SET status = ?, diagnostics = ? WHERE id = ?";
    private final BatchEntryDiagnosticsConversion batchEntryDiagnosticsConversion = new BatchEntryDiagnosticsConversion();
    private final BatchEntryMetadataConversion batchEntryMetadataConversion = new BatchEntryMetadataConversion();
    private final BatchEntryStatusConversion batchEntryStatusConversion = new BatchEntryStatusConversion();

    /**
     * Updates status and diagnostics fields of batch entry identified by given BatchEntry object
     * @param conn database connection
     * @param batchEntry BatchEntry object containing updated values
     * @return true if batch entry was updated, false if not
     * @throws SQLException in case of database interaction failure
     */
    public boolean updateBatchEntry(Connection conn, BatchEntry batchEntry) throws SQLException {
        try (PreparedStatement update = conn.prepareStatement(UPDATE_BATCH_ENTRY_STATEMENT)) {
            update.setObject(1, batchEntryStatusConversion.toDatabaseColumn(batchEntry.getStatus()));
            update.setObject(2, batchEntryDiagnosticsConversion.toDatabaseColumn(batchEntry.getDiagnostics()));
            update.setInt(3, batchEntry.getId());
            return update.executeUpdate() == 1;
        }
    }

    /**
     * Claims up to a maxNumberOfEntriesToClaim batch entries by setting their status to ACTIVE
     * @param conn database connection
     * @param maxNumberOfEntriesToClaim maximum number of batch entries to claim
     * @return list of claimed batch entries
     * @throws SQLException in case of database interaction failure
     */
    public List<BatchEntry> claimBatchEntries(Connection conn, int maxNumberOfEntriesToClaim) throws SQLException {
        try (PreparedStatement claim = conn.prepareStatement(BatchEntry.CLAIM_PENDING_ENTRIES_QUERY)) {
            claim.setInt(1, maxNumberOfEntriesToClaim);
            final ResultSet rs = claim.executeQuery();
            final List<BatchEntry> entries = new ArrayList<>();
            while (rs.next()) {
                final BatchEntry entry = new BatchEntry(rs.getInt(1), rs.getTimestamp(3), rs.getInt(6))
                        .withStatus(batchEntryStatusConversion.toEntityAttribute(rs.getObject(2)))
                        .withTimeOfCompletion(rs.getTimestamp(4))
                        .withTrackingId(rs.getString(5))
                        .withIsContinued(rs.getBoolean(7))
                        .withContent(rs.getBytes(8))
                        .withMetadata(batchEntryMetadataConversion.toEntityAttribute((PGobject) rs.getObject(9)))
                        .withDiagnostics(batchEntryDiagnosticsConversion.toEntityAttribute((PGobject) rs.getObject(10)));
                entries.add(entry);
            }
            return entries;
        }
    }

    /**
     * Resets claimed batch entries by restoring their state to PENDING
     * @param conn database connection
     * @return number of batch entries reset
     * @throws SQLException in case of database interaction failure
     */
    public int resetClaimedBatchEntries(Connection conn) throws SQLException {
        try (PreparedStatement reset = conn.prepareStatement(BatchEntry.RESET_CLAIMED_ENTRIES_QUERY)) {
            final ResultSet rs = reset.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }
}
