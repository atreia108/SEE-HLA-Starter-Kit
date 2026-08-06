package org.see.skf.callbacks;

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.SKUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HLACallbackManager {

    private static final Logger logger = LoggerFactory.getLogger(HLACallbackManager.class);

    private final RTIambassador rtiAmbassador;

    private final ExecutorService executor;

    private final Set<NameReservationCallback> nameReservationCallbacks;

    public HLACallbackManager(int maxThreads) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();
        this.executor = Executors.newFixedThreadPool(maxThreads);

        this.nameReservationCallbacks = new CopyOnWriteArraySet<>();
    }

    public Future<Boolean> invokeNameReservationCallback(String objectInstanceName) {
        NameReservationCallback callback = new NameReservationCallback(objectInstanceName, null);
        FutureTask<Boolean> task = callback.getTask();
        this.nameReservationCallbacks.add(callback);
        executor.submit(task);

        try {
            rtiAmbassador.reserveObjectInstanceName(objectInstanceName);
        } catch (IllegalName | SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new NameReservationException("The name <" + objectInstanceName + "> could not be reserved.", e);
        }

        return task;
    }

    public void completeNameReservationCallback(String objectInstanceName, boolean outcomeValue) {
        for (NameReservationCallback callback : this.nameReservationCallbacks) {
            if (callback.getTarget().equals(objectInstanceName)) {
                callback.complete(outcomeValue);
                nameReservationCallbacks.remove(callback);
            }
        }
    }
}
