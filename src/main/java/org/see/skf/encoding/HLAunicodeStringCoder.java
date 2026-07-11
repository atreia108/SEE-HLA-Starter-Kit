package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAunicodeString;

public class HLAunicodeStringCoder implements Coder<String> {

    private final HLAunicodeString unicodeString;

    public HLAunicodeStringCoder(EncoderFactory encoderFactory) {
        this.unicodeString = encoderFactory.createHLAunicodeString();
    }

    @Override
    public byte[] encode(String data) {
        unicodeString.setValue(data);
        return unicodeString.toByteArray();
    }

    @Override
    public String decode(byte[] bytes) throws DecoderException {
        unicodeString.decode(bytes);
        return unicodeString.getValue();
    }
}
