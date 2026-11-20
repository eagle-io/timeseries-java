package io.eagle.util.jts;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.util.DefaultPrettyPrinter;


/**
 * A {@link DefaultPrettyPrinter} that can be temporarily switched to single-line output (like a
 * {@link tools.jackson.core.util.MinimalPrettyPrinter}), so {@link JtsTableSerializer} can print each {@link JtsRecord} on its own line.
 * <p>
 * Jackson 3 no longer allows a serializer to replace the generator's pretty printer, so the switch is done via
 * {@link #setSingleLine(boolean)} on the printer installed by the {@link tools.jackson.databind.ObjectWriter}.
 */
public class JtsPrettyPrinter extends DefaultPrettyPrinter {

    private static final long serialVersionUID = 1L;

    private boolean singleLine;


    public JtsPrettyPrinter() {
        super();
    }


    protected JtsPrettyPrinter(JtsPrettyPrinter base) {
        super(base);
    }


    @Override
    public JtsPrettyPrinter createInstance() {
        return new JtsPrettyPrinter(this);
    }


    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
    }


    @Override
    public void writeStartObject(JsonGenerator g) throws JacksonException {
        if (singleLine)
            g.writeRaw('{');
        else
            super.writeStartObject(g);
    }


    @Override
    public void beforeObjectEntries(JsonGenerator g) throws JacksonException {
        if (!singleLine)
            super.beforeObjectEntries(g);
    }


    @Override
    public void writeObjectNameValueSeparator(JsonGenerator g) throws JacksonException {
        if (singleLine)
            g.writeRaw(':');
        else
            super.writeObjectNameValueSeparator(g);
    }


    @Override
    public void writeObjectEntrySeparator(JsonGenerator g) throws JacksonException {
        if (singleLine)
            g.writeRaw(',');
        else
            super.writeObjectEntrySeparator(g);
    }


    @Override
    public void writeEndObject(JsonGenerator g, int nrOfEntries) throws JacksonException {
        if (singleLine)
            g.writeRaw('}');
        else
            super.writeEndObject(g, nrOfEntries);
    }


    @Override
    public void writeStartArray(JsonGenerator g) throws JacksonException {
        if (singleLine)
            g.writeRaw('[');
        else
            super.writeStartArray(g);
    }


    @Override
    public void beforeArrayValues(JsonGenerator g) throws JacksonException {
        if (!singleLine)
            super.beforeArrayValues(g);
    }


    @Override
    public void writeArrayValueSeparator(JsonGenerator g) throws JacksonException {
        if (singleLine)
            g.writeRaw(',');
        else
            super.writeArrayValueSeparator(g);
    }


    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws JacksonException {
        if (singleLine)
            g.writeRaw(']');
        else
            super.writeEndArray(g, nrOfValues);
    }
}
