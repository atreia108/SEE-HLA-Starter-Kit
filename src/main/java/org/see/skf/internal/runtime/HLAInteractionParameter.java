package org.see.skf.internal.runtime;

import hla.rti1516_2025.ParameterHandle;

public final class HLAInteractionParameter {
    private final String name;
    private final ParameterHandle handle;

    public HLAInteractionParameter(String name, ParameterHandle handle) {
        this.name = name;
        this.handle = handle;
    }

    public String getName() {
        return name;
    }

    public ParameterHandle getHandle() {
        return handle;
    }
}
