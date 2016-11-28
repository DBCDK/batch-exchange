/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import org.junit.Test;
import org.postgresql.util.PGobject;

import static dk.dbc.commons.testutil.Assert.assertThat;
import static dk.dbc.commons.testutil.Assert.isThrowing;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class BatchStatusConverterTest {
    private final BatchStatusConverter converter = new BatchStatusConverter();

    @Test
    public void convertToDatabaseColumn_statusArgIsNull_returnNullValue() {
        final Object pgObject = converter.convertToDatabaseColumn(null);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", ((PGobject) pgObject).getType(), is("batch_status"));
        assertThat("PGobject value", ((PGobject) pgObject).getValue(), is(Batch.Status.PENDING.name()));
    }

    @Test
    public void convertToDatabaseColumn() {
        final Object pgObject = converter.convertToDatabaseColumn(Batch.Status.COMPLETED);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", ((PGobject) pgObject).getType(), is("batch_status"));
        assertThat("PGobject value", ((PGobject) pgObject).getValue(), is(Batch.Status.COMPLETED.name()));
    }

    @Test
    public void toEntityAttribute_dbValueArgIsNull_throws() {
        assertThat(() -> converter.convertToEntityAttribute(null), isThrowing(IllegalArgumentException.class));
    }

    @Test
    public void toEntityAttribute() {
        assertThat("PENDING", converter.convertToEntityAttribute("PENDING"), is(Batch.Status.PENDING));
        assertThat("COMPLETED", converter.convertToEntityAttribute("COMPLETED"), is(Batch.Status.COMPLETED));
        assertThat("UNKNOWN", converter.convertToEntityAttribute("UNKNOWN"), is(nullValue()));
    }
}