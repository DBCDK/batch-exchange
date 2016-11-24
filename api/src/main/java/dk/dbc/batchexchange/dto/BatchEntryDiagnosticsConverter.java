/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import org.postgresql.util.PGobject;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

@Converter
public class BatchEntryDiagnosticsConverter implements AttributeConverter<List<Diagnostic>, PGobject> {
    private static final BatchEntryDiagnosticsConversion CONVERSION = new BatchEntryDiagnosticsConversion();

    @Override
    public PGobject convertToDatabaseColumn(List<Diagnostic> diagnostics) {
        return CONVERSION.toDatabaseColumn(diagnostics);
    }

    @Override
    public List<Diagnostic> convertToEntityAttribute(PGobject pgObject) {
        return CONVERSION.toEntityAttribute(pgObject);
    }
}
