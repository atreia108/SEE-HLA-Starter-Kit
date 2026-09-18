package org.see.skf.core;

/**
 * Mode Transition Request (MTR) values. This enumeration is used to request a specific mode transition. However, not all
 * mode transition requests are accepted for any given Run Mode. Refer to the mode transition validation table in the
 * Space Reference FOM documentation.
 *
 * @since 1.0
 */
public enum MTRMode {
    MTR_UNDESIGNATED((short) -1),
    MTR_GOTO_RUN((short) 2),
    MTR_GOTO_FREEZE((short) 3),
    MTR_GOTO_SHUTDOWN((short) 4);

    private final short value;

    MTRMode(short value) {
        this.value = value;
    }

    public static MTRMode query(short value) {
        for (MTRMode  mode : MTRMode.values()) {
            if (mode.value == value) {
                return mode;
            }
        }

        return null;
    }

    public short getValue() {
        return this.value;
    }
}
