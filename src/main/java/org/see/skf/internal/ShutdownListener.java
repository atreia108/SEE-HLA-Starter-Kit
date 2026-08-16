package org.see.skf.internal;

import org.see.skf.core.ExecutionConfiguration;
import org.see.skf.core.ExecutionMode;
import org.see.skf.internal.executive.ExecutiveStateManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class ShutdownListener implements PropertyChangeListener {

    private final ExecutiveStateManager executiveStateManager;

    public ShutdownListener(ExecutiveStateManager executiveStateManager) {
        this.executiveStateManager = executiveStateManager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("next_execution_mode")) {
            ExecutionMode nextExecutionMode = (ExecutionMode) evt.getNewValue();
            this.executiveStateManager.changeExecutionMode(nextExecutionMode);
        }
    }
}
