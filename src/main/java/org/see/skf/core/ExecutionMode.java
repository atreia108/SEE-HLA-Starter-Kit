package org.see.skf.core;

public enum ExecutionMode {
    EXEC_MODE_UNDESIGNATED((short) -1),
    EXEC_MODE_UNINITIALIZED((short) 0),
    EXEC_MODE_INITIALIZING((short) 1),
    EXEC_MODE_RUNNING((short) 2),
    EXEC_MODE_FREEZE((short) 3),
    EXEC_MODE_SHUTDOWN((short) 4);

    private final short value;

    ExecutionMode(short value) {
        this.value = value;
    }

    public static ExecutionMode query(short value) {
        for (ExecutionMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }

        return null;
    }

    public short getValue() {
        return value;
    }
}
