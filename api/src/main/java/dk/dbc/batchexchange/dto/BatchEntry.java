/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name="entry")
@SqlResultSetMapping(name = BatchEntry.IMPLICIT_RESULT_SET_MAPPING, entities = {
    @EntityResult(entityClass = BatchEntry.class)}
)
@NamedNativeQueries({
    @NamedNativeQuery(name = BatchEntry.GET_BATCH_ENTRIES_QUERY_NAME,
        query = BatchEntry.GET_BATCH_ENTRIES_QUERY, resultSetMapping = BatchEntry.IMPLICIT_RESULT_SET_MAPPING),
    @NamedNativeQuery(name = BatchEntry.CLAIM_PENDING_ENTRIES_QUERY_NAME,
        query = BatchEntry.CLAIM_PENDING_ENTRIES_QUERY, resultSetMapping = BatchEntry.IMPLICIT_RESULT_SET_MAPPING),
    @NamedNativeQuery(name = BatchEntry.RESET_CLAIMED_ENTRIES_QUERY_NAME,
        query = BatchEntry.RESET_CLAIMED_ENTRIES_QUERY)
})
public class BatchEntry {
    /* Be advised that updating the internal state of a 'jsonb' column
       when used in ORM environment will not mark the field as dirty and
       therefore not result in a database update. The only way to achieve
       an update is to replace the field value with a new instance. */

    public static final String IMPLICIT_RESULT_SET_MAPPING = "BatchEntry.implicit";
    public static final String GET_BATCH_ENTRIES_QUERY = "SELECT * FROM get_batch_entries(?)";
    public static final String GET_BATCH_ENTRIES_QUERY_NAME = "get_batch_entries";
    public static final String CLAIM_PENDING_ENTRIES_QUERY = "SELECT * FROM claim_pending_entries(?) ORDER BY priority DESC, id ASC";
    public static final String CLAIM_PENDING_ENTRIES_QUERY_NAME = "claim_pending_entries";
    public static final String RESET_CLAIMED_ENTRIES_QUERY = "SELECT * FROM reset_claimed_entries()";
    public static final String RESET_CLAIMED_ENTRIES_QUERY_NAME = "reset_claimed_entries";

    public enum Status {
        PENDING,
        ACTIVE,
        OK,
        FAILED,
        IGNORED
    }

    @Id
    @SequenceGenerator(
            name = "entry_id_seq",
            sequenceName = "entry_id_seq",
            allocationSize = 1)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "entry_id_seq")
    @Column(updatable = false)
    private int id;

    @Convert(converter = BatchEntryStatusConverter.class)
    private Status status;

    @Column(insertable = false, updatable = false)
    private Timestamp timeOfCreation;

    private Timestamp timeOfCompletion;

    private String trackingId;

    private int batch;

    private boolean isContinued;

    private byte[] content;

    @Convert(converter = BatchEntryMetadataConverter.class)
    private String metadata;

    @Convert(converter = BatchEntryDiagnosticsConverter.class)
    private List<Diagnostic> diagnostics;

    private int priority;

    public BatchEntry() {}

    public BatchEntry(int id, Timestamp timeOfCreation, int batch) {
        this.id = id;
        this.timeOfCreation = timeOfCreation;
        this.batch = batch;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public BatchEntry withStatus(BatchEntry.Status status) {
        this.status = status;
        return this;
    }

    public Timestamp getTimeOfCreation() {
        return timeOfCreation;
    }

    public Timestamp getTimeOfCompletion() {
        return timeOfCompletion;
    }

    public BatchEntry withTimeOfCompletion(Timestamp timeOfCompletion) {
        this.timeOfCompletion = timeOfCompletion;
        return this;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public BatchEntry withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    public int getBatch() {
        return batch;
    }

    public BatchEntry withBatch(int batch) {
        this.batch = batch;
        return this;
    }

    public Boolean getContinued() {
        return isContinued;
    }

    public BatchEntry withIsContinued(boolean isContinued) {
        this.isContinued = isContinued;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public BatchEntry withContent(byte[] content) {
        this.content = content;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public BatchEntry withMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public BatchEntry withDiagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public BatchEntry withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public String toString() {
        return "BatchEntry{" +
                "id=" + id +
                ", status=" + status +
                ", timeOfCreation=" + timeOfCreation +
                ", timeOfCompletion=" + timeOfCompletion +
                ", trackingId='" + trackingId + '\'' +
                ", batch=" + batch +
                ", isContinued=" + isContinued +
                ", content=" + Arrays.toString(content) +
                ", metadata='" + metadata + '\'' +
                ", diagnostics=" + diagnostics +
                ", priority=" + priority +
                '}';
    }
}
