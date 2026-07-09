package org.see.skf.runtime;

public final class HLAInteractionData {
    private final Object data;

    public HLAInteractionData(Object data) {
        this.data = data;
    }

    public Object get() {
        return this.data;
    }
}
