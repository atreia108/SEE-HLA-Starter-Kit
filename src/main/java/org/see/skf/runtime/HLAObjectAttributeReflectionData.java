package org.see.skf.runtime;

import org.see.skf.encoding.Coder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class HLAObjectAttributeReflectionData {
    private final Field field;
    // private final Method getter;
    // private final Method setter;
    private final Coder<?> coder;

    public HLAObjectAttributeReflectionData(Field field, Coder<?> coder) {
        this.field = field;
        this.coder = coder;

        retrieveGetterSetter();
    }

    private void retrieveGetterSetter() {
        // TODO.
    }

    private byte[] encode() {
        return null;
    }

    private void decode(byte[] buffer) {

    }
}
