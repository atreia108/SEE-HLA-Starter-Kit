package org.see.skf.internal;

import org.see.skf.core.InteractionListener;
import org.see.skf.core.ObjectInstanceListener;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

public final class EventListenerManager {
    private final ExecutorService executor;

    private final Set<ObjectInstanceListener> objectInstanceListeners;
    private final Set<InteractionListener> interactionListeners;

    public EventListenerManager(ExecutorService executor) {
        this.executor = executor;

        this.objectInstanceListeners = new CopyOnWriteArraySet<>();
        this.interactionListeners = new CopyOnWriteArraySet<>();
    }

    public void addObjectInstanceListener(ObjectInstanceListener objectInstanceListener) {
        this.objectInstanceListeners.add(objectInstanceListener);
    }

    public void removeObjectInstanceListener(ObjectInstanceListener objectInstanceListener) {
        this.objectInstanceListeners.remove(objectInstanceListener);
    }

    public void addInteractionListener(InteractionListener interactionListener) {
        this.interactionListeners.add(interactionListener);
    }

    public void removeInteractionListener(InteractionListener interactionListener) {
        this.interactionListeners.remove(interactionListener);
    }
}
