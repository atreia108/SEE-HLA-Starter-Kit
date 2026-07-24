package org.see.skf.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public final class HLACallbackManager {
    private final Map<String, Future<Boolean>> pendingNameReservationCallbacks;

    HLACallbackManager() {
        this.pendingNameReservationCallbacks = new ConcurrentHashMap<>();
    }

    public Future<Boolean> invokeNameReservationCallback(String objectInstance) {
        return null;
    }

    void completeNameReservationCallback(String objectInstance) {
        // TODO...
    }

    public Future<Map<String, String>> invokeQueryAttributeOwnershipCallback(Object objectInstance, String... attributeNames) {
        return null;
    }

    void revealObjectInstanceAttributeOwner(String objectInstance, String... attributeNames) {

    }
}
