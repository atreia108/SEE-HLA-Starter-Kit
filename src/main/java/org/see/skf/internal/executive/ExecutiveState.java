package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.ExecutionMode;

public interface ExecutiveState {
    void update() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;
    void transition(ExecutionMode executionMode);
    ExecutionMode getExecutionMode();
}
