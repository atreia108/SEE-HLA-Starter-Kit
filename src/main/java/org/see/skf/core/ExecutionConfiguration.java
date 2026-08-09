package org.see.skf.core;

import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.ExecutionModeCoder;
import org.see.skf.encoding.HLAfloat64LECoder;
import org.see.skf.encoding.HLAinteger64BECoder;
import org.see.skf.encoding.HLAunicodeStringCoder;

@ObjectClass(name = "HLAobjectRoot.ExecutionConfiguration")
public final class ExecutionConfiguration {

    @Attribute(name = "root_frame_name", coder = HLAunicodeStringCoder.class)
    private String rootFrameName;

    @Attribute(name = "scenario_time_epoch", coder = HLAfloat64LECoder.class)
    private Double scenarioTimeEpoch;

    @Attribute(name = "current_execution_mode", coder = ExecutionModeCoder.class)
    private ExecutionMode currentExecutionMode;

    @Attribute(name = "next_execution_mode", coder = ExecutionModeCoder.class)
    private ExecutionMode nextExecutionMode;

    @Attribute(name = "next_mode_scenario_time", coder = HLAfloat64LECoder.class)
    private Double nextModeScenarioTime;

    @Attribute(name = "next_mode_cte_time", coder = HLAfloat64LECoder.class)
    private Double nextModeCTETime;

    @Attribute(name = "least_common_time_step", coder = HLAinteger64BECoder.class)
    private Long leastCommonTimeStep;

    public ExecutionConfiguration() {
        this.rootFrameName = "";
        this.currentExecutionMode = ExecutionMode.EXEC_MODE_UNDESIGNATED;
        this.nextExecutionMode = ExecutionMode.EXEC_MODE_UNDESIGNATED;
    }

    public String getRootFrameName() {
        return rootFrameName;
    }

    public void setRootFrameName(String rootFrameName) {
        this.rootFrameName = rootFrameName;
    }

    public Double getScenarioTimeEpoch() {
        return scenarioTimeEpoch;
    }

    public void setScenarioTimeEpoch(Double scenarioTimeEpoch) {
        this.scenarioTimeEpoch = scenarioTimeEpoch;
    }

    public ExecutionMode getCurrentExecutionMode() {
        return currentExecutionMode;
    }

    public void setCurrentExecutionMode(ExecutionMode currentExecutionMode) {
        this.currentExecutionMode = currentExecutionMode;
    }

    public ExecutionMode getNextExecutionMode() {
        return nextExecutionMode;
    }

    public void setNextExecutionMode(ExecutionMode nextExecutionMode) {
        this.nextExecutionMode = nextExecutionMode;
    }

    public Double getNextModeScenarioTime() {
        return nextModeScenarioTime;
    }

    public void setNextModeScenarioTime(Double nextModeScenarioTime) {
        this.nextModeScenarioTime = nextModeScenarioTime;
    }

    public Double getNextModeCTETime() {
        return nextModeCTETime;
    }

    public void setNextModeCTETime(Double nextModeCTETime) {
        this.nextModeCTETime = nextModeCTETime;
    }

    public Long getLeastCommonTimeStep() {
        return leastCommonTimeStep;
    }

    public void setLeastCommonTimeStep(Long leastCommonTimeStep) {
        this.leastCommonTimeStep = leastCommonTimeStep;
    }

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
}
