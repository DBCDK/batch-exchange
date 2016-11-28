/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import org.junit.Test;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class BatchEntryMetadataConverterTest {
    private final BatchEntryMetadataConverter converter = new BatchEntryMetadataConverter();

    @Test
    public void convertToDatabaseColumn_metadataArgIsNull_returnsNullValue() {
        final PGobject pgObject = converter.convertToDatabaseColumn(null);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", pgObject.getType(), is("jsonb"));
        assertThat("PGobject value", pgObject.getValue(), is(nullValue()));
    }

    @Test
    public void convertToDatabaseColumn_metadataArgIsEmpty_returnsNullValue() {
        final PGobject pgObject = converter.convertToDatabaseColumn(null);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", pgObject.getType(), is("jsonb"));
        assertThat("PGobject value", pgObject.getValue(), is(nullValue()));
    }

    @Test
    public void convertToDatabaseColumn() {
        final String metadata = "{\"key\": \"value\"}";
        final PGobject pgObject = converter.convertToDatabaseColumn(metadata);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", pgObject.getType(), is("jsonb"));
        assertThat("PGobject value", pgObject.getValue(), is(metadata));
    }

    @Test
    public void toEntityAttribute_pgObjectArgIsNull_returnsNull() {
        assertThat(converter.convertToEntityAttribute(null), is(nullValue()));
    }

    @Test
    public void toEntityAttribute() throws SQLException {
        final String expectedMetadata = "{\"key\": \"value\"}";
        final PGobject pgObject = new PGobject();
        pgObject.setValue(expectedMetadata);

        final String metadata = converter.convertToEntityAttribute(pgObject);
        assertThat("metadata", metadata, is(notNullValue()));
        assertThat("metadata value", metadata, is(expectedMetadata));
    }
}