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

public class BatchEntryStatusConverterTest {
    private final BatchEntryStatusConverter converter = new BatchEntryStatusConverter();

    @Test
    public void convertToDatabaseColumn_statusArgIsNull_throws() {
        assertThat(() -> converter.convertToDatabaseColumn(null), isThrowing(IllegalArgumentException.class));
    }

    @Test
    public void convertToDatabaseColumn() {
        final Object pgObject = converter.convertToDatabaseColumn(BatchEntry.Status.ACTIVE);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", ((PGobject) pgObject).getType(), is("entry_status"));
        assertThat("PGobject value", ((PGobject) pgObject).getValue(), is(BatchEntry.Status.ACTIVE.name()));
    }

    @Test
    public void toEntityAttribute_dbValueArgIsNull_throws() {
        assertThat(() -> converter.convertToEntityAttribute(null), isThrowing(IllegalArgumentException.class));
    }

    @Test
    public void toEntityAttribute() {
        assertThat("PENDING", converter.convertToEntityAttribute("PENDING"), is(BatchEntry.Status.PENDING));
        assertThat("ACTIVE", converter.convertToEntityAttribute("ACTIVE"), is(BatchEntry.Status.ACTIVE));
        assertThat("OK", converter.convertToEntityAttribute("OK"), is(BatchEntry.Status.OK));
        assertThat("FAILED", converter.convertToEntityAttribute("FAILED"), is(BatchEntry.Status.FAILED));
        assertThat("IGNORED", converter.convertToEntityAttribute("IGNORED"), is(BatchEntry.Status.IGNORED));
        assertThat("UNKNOWN", converter.convertToEntityAttribute("UNKNOWN"), is(nullValue()));
    }
}