/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class BatchEntryMetadataConversion {
    public PGobject toDatabaseColumn(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        final PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        try {
            pgObject.setValue(metadata);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return pgObject;
    }

    public String toEntityAttribute(PGobject pgObject) {
        if (pgObject == null) {
            return null;
        }
        return pgObject.getValue();
    }
}
