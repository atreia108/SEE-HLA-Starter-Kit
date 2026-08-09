package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;

public class InvalidCoder implements Coder<Object> {

    @Override
    public byte[] encode(Object data) {
        return new byte[0];
    }

    @Override
    public Object decode(byte[] data) throws DecoderException {
        return null;
    }
}
