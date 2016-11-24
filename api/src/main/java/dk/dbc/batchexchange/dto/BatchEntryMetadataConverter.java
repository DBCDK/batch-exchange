/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import org.postgresql.util.PGobject;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BatchEntryMetadataConverter implements AttributeConverter<String, PGobject> {
    private static final BatchEntryMetadataConversion CONVERSION = new BatchEntryMetadataConversion();

    @Override
    public PGobject convertToDatabaseColumn(String metadata) {
        return CONVERSION.toDatabaseColumn(metadata);
    }

    @Override
    public String convertToEntityAttribute(PGobject pgObject) {
        return CONVERSION.toEntityAttribute(pgObject);
    }
}
