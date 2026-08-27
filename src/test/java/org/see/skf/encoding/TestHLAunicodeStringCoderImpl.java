package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAunicodeString;

public class TestHLAunicodeStringCoderImpl implements Coder<String> {

    private final HLAunicodeString stringType;

    public TestHLAunicodeStringCoderImpl(EncoderFactory encoderFactory) {
        this.stringType = encoderFactory.createHLAunicodeString();
    }

    @Override
    public byte[] encode(String data) {
        this.stringType.setValue(data);
        return this.stringType.toByteArray();
    }

    @Override
    public String decode(byte[] bytes) throws DecoderException {
        this.stringType.decode(bytes);
        return this.stringType.getValue();
    }
}
