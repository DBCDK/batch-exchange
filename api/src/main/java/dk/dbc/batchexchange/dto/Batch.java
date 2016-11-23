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
import java.sql.Timestamp;

@Entity
@SqlResultSetMapping(name = Batch.IMPLICIT_RESULT_SET_MAPPING, entities = {
    @EntityResult(entityClass = Batch.class)}
)
@NamedNativeQueries({
    @NamedNativeQuery(name = Batch.GET_COMPLETED_BATCH_QUERY_NAME,
        query = Batch.GET_COMPLETED_BATCH_QUERY, resultSetMapping = Batch.IMPLICIT_RESULT_SET_MAPPING)
})
public class Batch {
    public static final String IMPLICIT_RESULT_SET_MAPPING = "Batch.implicit";
    public static final String GET_COMPLETED_BATCH_QUERY = "SELECT * FROM get_completed_batch()";
    public static final String GET_COMPLETED_BATCH_QUERY_NAME = "get_completed_batch";

    public enum Status {
        PENDING,
        COMPLETED
    }

    @Id
    @SequenceGenerator(
            name = "batch_id_seq",
            sequenceName = "batch_id_seq",
            allocationSize = 1)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "batch_id_seq")
    @Column(updatable = false)
    private int id;

    private String name;

    @Convert(converter = BatchStatusConverter.class)
    private Status status;

    @Column(insertable = false, updatable = false)
    private Timestamp timeOfCreation;

    @Column(insertable = false, updatable = false)
    private int incompleteEntries;

    public Batch() {}

    public Batch(int id, Timestamp timeOfCreation, int incompleteEntries) {
        this.id = id;
        this.timeOfCreation = timeOfCreation;
        this.incompleteEntries = incompleteEntries;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Batch withName(String name) {
        this.name = name;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public Batch withStatus(Status status) {
        this.status = status;
        return this;
    }

    public Timestamp getTimeOfCreation() {
        return timeOfCreation;
    }

    public int getIncompleteEntries() {
        return incompleteEntries;
    }

    @Override
    public String toString() {
        return "Batch{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", timeOfCreation=" + timeOfCreation +
                ", incompleteEntries=" + incompleteEntries +
                '}';
    }
}
