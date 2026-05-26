package org.see.skf.runtime;

public interface Coder<T> {
    void encode(T data);
    T decode (byte[] data);
}
