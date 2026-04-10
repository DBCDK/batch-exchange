package dk.dbc.batchexchange.dto;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(null));
    }

    @Test
    public void toEntityAttribute() {
        assertThat("PENDING", converter.convertToEntityAttribute("PENDING"), is(Batch.Status.PENDING));
        assertThat("COMPLETED", converter.convertToEntityAttribute("COMPLETED"), is(Batch.Status.COMPLETED));
        assertThat("UNKNOWN", converter.convertToEntityAttribute("UNKNOWN"), is(nullValue()));
    }
}