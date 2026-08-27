package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAboolean;

public class TestHLAbooleanCoderImpl implements Coder<Boolean> {

    private final HLAboolean booleanType;

    public TestHLAbooleanCoderImpl(EncoderFactory encoderFactory) {
        this.booleanType = encoderFactory.createHLAboolean();
    }

    @Override
    public byte[] encode(Boolean data) {
        this.booleanType.setValue(data);
        return this.booleanType.toByteArray();
    }

    @Override
    public Boolean decode(byte[] bytes) throws DecoderException {
        this.booleanType.decode(bytes);
        return this.booleanType.getValue();
    }
}
