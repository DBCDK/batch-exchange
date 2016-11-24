/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import org.junit.Test;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class BatchEntryDiagnosticsConverterTest {
    private final BatchEntryDiagnosticsConverter converter = new BatchEntryDiagnosticsConverter();

    @Test
    public void convertToDatabaseColumn_diagnosticArgIsNull_returnsEmptyJsonArray() {
        final PGobject pgObject = converter.convertToDatabaseColumn(null);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", pgObject.getType(), is("jsonb"));
        assertThat("PGobject value", pgObject.getValue(), is("[]"));
    }

    @Test
    public void convertToDatabaseColumn_diagnosticArgIsEmpty_returnsEmptyJsonArray() {
        final PGobject pgObject = converter.convertToDatabaseColumn(Collections.emptyList());
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", pgObject.getType(), is("jsonb"));
        assertThat("PGobject value", pgObject.getValue(), is("[]"));
    }

    @Test
    public void convertToDatabaseColumn() {
        final List<Diagnostic> diagnostics = Arrays.asList(
                Diagnostic.createError("err"),
                Diagnostic.createWarning("warn"));
        final PGobject pgObject = converter.convertToDatabaseColumn(diagnostics);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", pgObject.getType(), is("jsonb"));
        assertThat("PGobject value", pgObject.getValue(),
                is("[{\"level\":\"ERROR\",\"message\":\"err\"},{\"level\":\"WARNING\",\"message\":\"warn\"}]"));
    }

    @Test
    public void toEntityAttribute_pgObjectArgIsNull_returnsEmptyList() {
        final List<Diagnostic> diagnostics = converter.convertToEntityAttribute(null);
        assertThat("diagnostics", diagnostics, is(notNullValue()));
        assertThat("diagnostics is empty", diagnostics.isEmpty(), is(true));
    }

    @Test
    public void toEntityAttribute() throws SQLException {
        final List<Diagnostic> expectedDiagnostics = Arrays.asList(
                Diagnostic.createError("err"),
                Diagnostic.createWarning("warn"));
        final PGobject pgObject = new PGobject();
        pgObject.setValue("[{\"level\":\"ERROR\",\"message\":\"err\"},{\"level\":\"WARNING\",\"message\":\"warn\"}]");

        final List<Diagnostic> diagnostics = converter.convertToEntityAttribute(pgObject);
        assertThat("diagnostics", diagnostics, is(notNullValue()));
        assertThat("diagnostics value", diagnostics, is(expectedDiagnostics));
    }
}