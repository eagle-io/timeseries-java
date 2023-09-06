package io.eagle.util.jts;

import java.util.BitSet;

/**
 * Stores a 31-bit compound value representing the quality of a timeseries sample.
 * <p>
 * Bit 0-10		RESERVED
 * Bit 11-15	HISTORIAN BITS
 * Bit 16-31	USER QUALITY
 *
 * @author <a href="mailto:jarrah@argos.io">Jarrah Watson</a>
 */
public class Quality {

    private static final int ORIGIN_MSB = 1;
    private static final int ORIGIN_LSB = 0;
    private static final int PARTIAL = 2;
    private static final int EXTRA_DATA = 3;
    private static final int MULTI_VALUE = 4;
    private final char userQuality;
    private BitSet historianBits = new BitSet( 5 );


    public Quality( Quality quality ) {
        this.historianBits = quality.historianBits;
        this.userQuality = quality.userQuality;
    }

    public Quality( int userQuality ) {
        this.userQuality = (char) userQuality;
    }

    private Quality( Builder builder ) {
        this.historianBits = builder.historianBits;
        this.userQuality = builder.userQuality;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append( String.format( "%-25s '%s'\n", "Quality origin:", this.getOrigin() ) );
        sb.append( String.format( "%-25s '%s'\n", "Quality partial:", this.getPartial() ) );
        sb.append( String.format( "%-25s '%s'\n", "Quality extraData:", this.getExtraData() ) );
        sb.append( String.format( "%-25s '%s'\n", "Quality multiValue:", this.getMultiValue() ) );
        sb.append( String.format( "%-25s '%s'\n", "Quality userQuality:", this.getUserQuality() ) );
        sb.append( String.format( "%-25s '%s'\n", "Quality Bit String:", Integer.toBinaryString( getQuality() ) ) );

        return sb.toString();
    }

    public Origin getOrigin() {
        if( historianBits.get( ORIGIN_MSB ) == false && historianBits.get( ORIGIN_LSB ) == true )
            return Origin.CALCULATED;
        else if( historianBits.get( ORIGIN_MSB ) == true && historianBits.get( ORIGIN_LSB ) == false )
            return Origin.INTERPOLATED;
        else if( historianBits.get( ORIGIN_MSB ) == false && historianBits.get( ORIGIN_LSB ) == false )
            return Origin.RAW;
        else
            throw new IllegalStateException( "Invalid origin" );
    }

    public boolean getPartial() {
        return this.historianBits.get( PARTIAL );
    }

    public boolean getExtraData() {
        return this.historianBits.get( EXTRA_DATA );
    }

    public boolean getMultiValue() {
        return this.historianBits.get( MULTI_VALUE );
    }

    public BitSet getHistorianBits() {
        return this.historianBits;
    }

    public int getUserQuality() {
        return this.userQuality;
    }

    public int getQuality() {
        byte historianByte = historianBits.toByteArray()[0];
        return ( historianByte & 0xFFFF ) << 16 | ( userQuality & 0xFFFF );
    }

    public static class Builder {

        private final BitSet historianBits = new BitSet( 5 );
        private char userQuality;


        public Builder origin( Origin origin ) {
            switch( origin ) {
                case CALCULATED:
                    this.historianBits.set( ORIGIN_MSB, false );
                    this.historianBits.set( ORIGIN_LSB, true );
                    break;
                case INTERPOLATED:
                    this.historianBits.set( ORIGIN_MSB, true );
                    this.historianBits.set( ORIGIN_LSB, false );
                    break;
                case RAW:
                    this.historianBits.set( ORIGIN_MSB, false );
                    this.historianBits.set( ORIGIN_LSB, false );
                    break;
            }

            return this;
        }

        public Builder partial( boolean partial ) {
            this.historianBits.set( PARTIAL, partial );
            return this;
        }

        public Builder extraData( boolean extraData ) {
            this.historianBits.set( EXTRA_DATA, extraData );
            return this;
        }

        public Builder multiValue( boolean multiValue ) {
            this.historianBits.set( MULTI_VALUE, multiValue );
            return this;
        }

        public Builder userQuality( int userQuality ) {
            this.userQuality = (char) userQuality;
            return this;
        }

        public Quality build() {
            return new Quality( this );
        }
    }
}
