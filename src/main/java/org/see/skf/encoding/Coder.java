package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;

public interface Coder<T> {
    byte[] encode(T data);
    T decode (byte[] data) throws DecoderException;
}
