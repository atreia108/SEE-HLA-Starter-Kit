package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAboolean;

public class HLAbooleanCoder implements Coder<Boolean> {

    private final HLAboolean booleanType;

    public HLAbooleanCoder(EncoderFactory encoderFactory) {
        this.booleanType = encoderFactory.createHLAboolean();
    }

    @Override
    public byte[] encode(Boolean data) {
        booleanType.setValue(data);
        return booleanType.toByteArray();
    }

    @Override
    public Boolean decode(byte[] bytes) throws DecoderException {
        booleanType.decode(bytes);
        return booleanType.getValue();
    }
}
