package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.ExecutionMode;

interface TransitiveState {
    void transition(ExecutionMode nextExecutionMode) throws RTIexception;
}
