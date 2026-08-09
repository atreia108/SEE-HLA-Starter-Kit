package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAinteger64BE;

public class HLAinteger64BECoder implements Coder<Long> {

    private final HLAinteger64BE longType;

    public HLAinteger64BECoder(EncoderFactory encoderFactory) {
        this.longType = encoderFactory.createHLAinteger64BE();
    }

    @Override
    public byte[] encode(Long data) {
        this.longType.setValue(data);
        return this.longType.toByteArray();
    }

    @Override
    public Long decode(byte[] data) throws DecoderException {
        this.longType.decode(data);
        return this.longType.getValue();
    }
}
