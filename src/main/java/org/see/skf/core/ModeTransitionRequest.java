package org.see.skf.core;

import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.Parameter;
import org.see.skf.encoding.MTRModeCoder;

/**
 * Representation of the ModeTransitionRequest interaction class in the SpaceFOM.
 *
 * @since 1.5
 */
@InteractionClass(name = "HLAinteractionRoot.ModeTransitionRequest")
public final class ModeTransitionRequest {

    @Parameter(name = "execution_mode", coder = MTRModeCoder.class)
    private MTRMode executionMode;

    public ModeTransitionRequest() {
        /* Zero-arg constructor as required by the framework and the JavaBeans standard. */
        this.executionMode = MTRMode.MTR_UNDESIGNATED;
    }

    public MTRMode getExecutionMode() {
        return this.executionMode;
    }

    public void setExecutionMode(MTRMode executionMode) {
        this.executionMode = executionMode;
    }

    /**
     * The run mode that can be requested by a federate other than the Master federate. There are only 3 valid Mode Transition Request (MTR) mode values: MTR_GOTO_RUN, MTR_GOTO_FREEZE, MTR_GOTO_SHUTDOWN.
     * Of these three valid mode requests, only 7 combinations of current execution mode and requested mode are valid:
     * <ol>
     *     <li>EXEC_MODE_UNINITIALIZED -&gt; EXEC_MODE_SHUTDOWN</li>
     *     <li>EXEC_MODE_INITIALIZED -&gt; EXEC_MODE_FREEZE</li>
     *     <li>EXEC_MODE_INITIALIZED -&gt; EXEC_MODE_SHUTDOWN</li>
     *     <li>EXEC_MODE_RUNNING -&gt; EXEC_MODE_FREEZE</li>
     *     <li>EXEC_MODE_RUNNING -&gt; EXEC_MODE_SHUTDOWN</li>
     *     <li>EXEC_MODE_FREEZE -&gt; EXEC_MODE_RUNNING</li>
     *     <li>EXEC_MODE_FREEZE -&gt; EXEC_MODE_SHUTDOWN</li>
     * </ol>
     *
     * @since 1.5
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
}
