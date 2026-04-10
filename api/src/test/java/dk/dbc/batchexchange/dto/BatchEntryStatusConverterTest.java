package dk.dbc.batchexchange.dto;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BatchEntryStatusConverterTest {
    private final BatchEntryStatusConverter converter = new BatchEntryStatusConverter();

    @Test
    public void convertToDatabaseColumn_statusArgIsNull_throws() {
        final Object pgObject = converter.convertToDatabaseColumn(null);
        assertThat("PGobject", pgObject, is(notNullValue()));
        assertThat("PGobject type", ((PGobject) pgObject).getType(), is("entry_status"));
        assertThat("PGobject value", ((PGobject) pgObject).getValue(), is(BatchEntry.Status.PENDING.name()));
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
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(null));
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