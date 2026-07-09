package org.see.skf.encoding;

public interface Coder<T> {
    void encode(T data);
    T decode (byte[] data);
}
