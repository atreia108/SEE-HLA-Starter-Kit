package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAunicodeString;

public class HLAunicodeStringCoder implements Coder<String> {

    private final HLAunicodeString stringType;

    public HLAunicodeStringCoder(EncoderFactory encoderFactory) {
        this.stringType = encoderFactory.createHLAunicodeString();
    }

    @Override
    public byte[] encode(String data) {
        stringType.setValue(data);
        return stringType.toByteArray();
    }

    @Override
    public String decode(byte[] bytes) throws DecoderException {
        stringType.decode(bytes);
        return stringType.getValue();
    }
}
