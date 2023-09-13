/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BatchStatusConverter implements AttributeConverter<Batch.Status, Object> {
    private static final BatchStatusConversion CONVERSION = new BatchStatusConversion();

    @Override
    public Object convertToDatabaseColumn(Batch.Status status) {
        return CONVERSION.toDatabaseColumn(status);
    }

    @Override
    public Batch.Status convertToEntityAttribute(Object dbValue) {
        return CONVERSION.toEntityAttribute(dbValue);
    }
}
