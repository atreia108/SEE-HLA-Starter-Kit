package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAfloat64LE;

public class HLAfloat64LECoder implements Coder<Double> {

    private final HLAfloat64LE float64LEType;

    public HLAfloat64LECoder(EncoderFactory encoderFactory) {
        this.float64LEType = encoderFactory.createHLAfloat64LE();
    }

    @Override
    public byte[] encode(Double data) {
        float64LEType.setValue(data);
        return float64LEType.toByteArray();
    }

    @Override
    public Double decode(byte[] data) throws DecoderException {
        float64LEType.decode(data);
        return float64LEType.getValue();
    }
}
