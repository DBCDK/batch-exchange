/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class BatchEntryStatusConversion {
    public Object toDatabaseColumn(BatchEntry.Status status) {
        if (status == null) {
            status = BatchEntry.Status.PENDING;
        }
        final PGobject pgObject = new PGobject();
        pgObject.setType("entry_status");
        try {
            pgObject.setValue(status.name());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return pgObject;
    }

    public BatchEntry.Status toEntityAttribute(Object dbValue) {
        if (dbValue == null) {
            throw new IllegalArgumentException("dbValue can not be null");
        }
        switch ((String) dbValue) {
            case "PENDING": return BatchEntry.Status.PENDING;
            case "ACTIVE": return BatchEntry.Status.ACTIVE;
            case "OK": return BatchEntry.Status.OK;
            case "FAILED": return BatchEntry.Status.FAILED;
            case "IGNORED": return BatchEntry.Status.IGNORED;
            default: return null;
        }
    }
}
