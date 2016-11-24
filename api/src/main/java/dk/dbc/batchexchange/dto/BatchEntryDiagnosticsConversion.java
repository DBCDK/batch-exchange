/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class BatchEntryDiagnosticsConversion {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CollectionType entityAttributeType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, Diagnostic.class);

    public PGobject toDatabaseColumn(List<Diagnostic> diagnostics) {
        if (diagnostics == null) {
            diagnostics = Collections.emptyList();
        }
        final PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        try {
            pgObject.setValue(marshall(diagnostics));
        } catch (IOException | SQLException e) {
            throw new IllegalStateException(e);
        }
        return pgObject;
    }

    public List<Diagnostic> toEntityAttribute(PGobject pgObject) {
        if (pgObject == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(pgObject.getValue(), entityAttributeType);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String marshall(Object object) throws IOException {
        final StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter, object);
        return stringWriter.toString();
    }
}
