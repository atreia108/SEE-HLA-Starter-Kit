package org.see.skf.internal.runtime;

import hla.rti1516_2025.encoding.DecoderException;
import org.see.skf.encoding.Coder;

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
