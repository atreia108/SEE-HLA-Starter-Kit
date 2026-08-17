package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.ExecutionMode;

interface SRFOMTransitiveState {
    void transition(ExecutionMode nextExecutionMode) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;
}
