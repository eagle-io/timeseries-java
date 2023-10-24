package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Iterables;
import io.eagle.util.jackson.JacksonUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Set;


public class JtsProfile implements JtsData {

    @JsonIgnore
    private final ArrayTable<Integer, DateTime, PosVal> table;

    public JtsProfile( Iterable<Integer> indexes, Iterable<DateTime> timestamps ) {
        this.table = ArrayTable.create( indexes, timestamps );
    }

    public void put( Integer index, DateTime ts, Double pos, Double val ) {
        this.table.put( index, ts, new PosVal( pos, val ) );
    }

    public boolean isEmpty() {
        return this.table.isEmpty();
    }

    public int recordCount() {
        return this.table.rowKeySet().size();
    }

    public int fieldCount() { return this.table.size(); }

    public Set<DateTime> getProfileTimes() {
        return this.table.columnKeySet();
    }

    public Double getPos( Integer index, DateTime ts ) {
        return this.table.get( index, ts ).pos;
    }

    public Double getVal( Integer index, DateTime ts ) {
        return this.table.get( index, ts ).val;
    }

    /* 	Positions: 3, Times: 3

        1970-01-01T10:00:00.001+10:00   1970-01-01T10:00:00.002+10:00   1970-01-01T10:00:00.003+10:00
        499.9958->77.7                  499.6345->77.7                  499.6923->77.7
        999.0498->77.7                  999.1326->77.7                  999.4966->77.7
        1499.0495->77.7                 1499.2856->77.7                 1499.4498->77.7
     */
    @Override
    public final String toString() {
        int columnWidth = 32;
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" );
        sb.append( String.format( "Positions: %d, Times: %d\n", this.recordCount(), this.table.columnKeySet().size() ) );
        sb.append( String.format( "%-32s\n", "" ) );

        // Header row, print timestamps
        for( final DateTime ts : this.table.columnKeySet() )
            sb.append( String.format( "%-" + columnWidth + "s", ts ) );

        sb.append( "\n" );

        for( int row = 0; row < this.table.rowKeySet().size(); row++ ) {
            for( DateTime ts : this.table.columnKeySet() )
                sb.append( String.format( "%-" + columnWidth + "s", this.table.get( row, ts ) ) );

            sb.append( "\n" );
        }

        return sb.toString();
    }

    public String toDelimitedText( JtsDocumentHeader header, DocumentFormat documentFormat, DateTimeZone timezone ) {
        // TODO Auto-generated method stub
        return null;
    }

    public String toFixedWidth( JtsDocumentHeader header, DocumentFormat documentFormat, DateTimeZone timezone ) {
        // TODO Auto-generated method stub
        return null;
    }

    @JsonValue
    public final String toJson() {
        return JacksonUtil.toJson( this );
    }

    public DateTime getFirstTimestamp() {
        return Iterables.getFirst( this.table.columnKeySet(), null );
    }

    public DateTime getLastTimestamp() {
        return Iterables.getLast( this.table.columnKeySet(), null );
    }

    public class PosVal {

        public Double pos;
        public Double val;

        public PosVal( Double pos, Double val ) {
            this.pos = pos;
            this.val = val;
        }


        public String toString() {
            return String.format( "%.3f->%.3f", pos, val );
        }
    }
}
