/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BatchEntryStatusConverter implements AttributeConverter<BatchEntry.Status, Object> {
    private static final BatchEntryStatusConversion CONVERSION = new BatchEntryStatusConversion();

    @Override
    public Object convertToDatabaseColumn(BatchEntry.Status status) {
        return CONVERSION.toDatabaseColumn(status);
    }

    @Override
    public BatchEntry.Status convertToEntityAttribute(Object dbValue) {
        return CONVERSION.toEntityAttribute(dbValue);
    }
}
