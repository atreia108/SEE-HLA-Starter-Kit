package org.see.skf.internal.executive;

import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKFederateBase;
import org.see.skf.internal.TimeManager;

public final class FreezeState implements SRFOMTransitiveState {

    private final SKFederateBase federate;
    private final TimeManager timeManager;

    public FreezeState(SKFederateBase federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
    }

    @Override
    public void transition(ExecutionMode nextExecutionMode) {

    }
}
