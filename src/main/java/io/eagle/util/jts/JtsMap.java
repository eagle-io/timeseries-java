package io.eagle.util.jts;

import com.google.common.collect.ForwardingSortedMap;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.SortedMap;


public class JtsMap extends ForwardingSortedMap<DateTime, JtsField> {

    private final TypeChecker typeChecker;
    private final SortedMap<DateTime, JtsField> records;


    public JtsMap( SortedMap<DateTime, JtsField> records ) {
        this( records, null );
    }


    public JtsMap( SortedMap<DateTime, JtsField> records, TypeChecker typeChecker ) {
        this.typeChecker = typeChecker;
        this.records = records;
    }


    @Override
    protected SortedMap<DateTime, JtsField> delegate() {
        return this.records;
    }


    @Override
    public JtsField put( DateTime ts, JtsField field ) {
        if( this.typeChecker != null )
            typeChecker.checkType( field.getDataType() );

        return delegate().put( ts, field );
    }


    @Override
    public void putAll( Map<? extends DateTime, ? extends JtsField> records ) {
        for( DateTime ts : records.keySet() )
            put( ts, records.get( ts ) );
    }

}
