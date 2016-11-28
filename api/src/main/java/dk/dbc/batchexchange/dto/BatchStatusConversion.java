/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class BatchStatusConversion {
    public Object toDatabaseColumn(Batch.Status status) {
        if (status == null) {
            status = Batch.Status.PENDING;
        }
        final PGobject pgObject = new PGobject();
        pgObject.setType("batch_status");
        try {
            pgObject.setValue(status.name());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return pgObject;
    }

    public Batch.Status toEntityAttribute(Object dbValue) {
        if (dbValue == null) {
            throw new IllegalArgumentException("dbValue can not be null");
        }
        switch ((String) dbValue) {
            case "PENDING": return Batch.Status.PENDING;
            case "COMPLETED": return Batch.Status.COMPLETED;
            default: return null;
        }
    }
}
