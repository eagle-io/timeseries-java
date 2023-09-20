package io.eagle.util.jts.complex;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.eagle.util.geo.Coordinates;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import java.util.Objects;

/**
 * Any object which can describe itself with a type identifier, for example {@code $foobar}.
 *
 * T is the target serialization type.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(name = Coordinates.COORDS_KEY,  value = Coordinates.class),
        @JsonSubTypes.Type(name = Metrics.METRICS_KEY, value = Metrics.class),
        @JsonSubTypes.Type(names = {Time.TIME_KEY, Time.MILLIS_KEY}, value = Time.class)
})
public abstract class ComplexValue<T> {

    protected T value;

    private ComplexValue() {}

    public ComplexValue(T value) {
        this.value = value;
    }

    public abstract String getKey();

    public T getValue() {
        return this.value;
    }

    public BSONObject toBson() {
        return new BasicBSONObject(getKey(), this.value);
    }

    @JsonValue
    public Object toJson() {
        return value;
    }

    public String toDelimitedText(final String delimiter, final String textQualifier) {
        return String.valueOf(this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexValue<?> that = (ComplexValue<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
