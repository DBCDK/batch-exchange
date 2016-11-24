/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Diagnostic {
    public enum Level { ERROR, WARNING }

    private final Level level;
    private final String message;

    public static Diagnostic createError(String message) {
        return new Diagnostic(Level.ERROR, message);
    }

    public static Diagnostic createWarning(String message) {
        return new Diagnostic(Level.WARNING, message);
    }

    @JsonCreator
    public Diagnostic(@JsonProperty("level") Level level,
                      @JsonProperty("message") String message) {
        if (level == null) {
            throw new IllegalArgumentException("level can not be null");
        }
        this.level = level;
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("message can not be null or empty '" + message + "'");
        }
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Diagnostic{" +
                "level=" + level +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Diagnostic that = (Diagnostic) o;

        if (level != that.level) {
            return false;
        }
        return message.equals(that.message);

    }

    @Override
    public int hashCode() {
        int result = level.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }
}
