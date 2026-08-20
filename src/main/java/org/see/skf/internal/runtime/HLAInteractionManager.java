package org.see.skf.internal.runtime;

import hla.rti1516_2025.InteractionClassHandle;
import hla.rti1516_2025.ParameterHandleValueMap;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.core.InteractionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class HLAInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(HLAInteractionManager.class);

    private final RTIambassador rtiAmbassador;

    private final SKAnnotatedTypeParser2 parser;

    private final Set<HLAInteractionClass> interactionClasses;
    private final Set<InteractionListener> interactionListeners;

    public HLAInteractionManager(SKAnnotatedTypeParser2 parser) {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.parser = parser;

        this.interactionClasses = new CopyOnWriteArraySet<>();
        this.interactionListeners = new CopyOnWriteArraySet<>();
    }

    private HLAInteractionClass getInteractionClass(InteractionClassHandle handle) {
        return this.interactionClasses.stream()
                .filter(i -> i.getHandle().equals(handle))
                .findFirst()
                .orElse(null);
    }

    private HLAInteractionClass getInteractionClass(String name) {
        return this.interactionClasses.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private HLAInteractionClass getInteractionClass(Class<?> proxyClass) {
        return this.interactionClasses.stream()
                .filter(i -> i.getProxyClass().equals(proxyClass))
                .findFirst()
                .orElse(null);
    }

    private HLAInteractionClass createInteractionClass(String name, Class<?> proxyClass) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        SKAnnotatedTypeParser2.Metadata proxyMetadata = this.parser.parseInteractionProxy(proxyClass);

        InteractionClassHandle handle;
        try {
            handle = rtiAmbassador.getInteractionClassHandle(name);
        } catch (NameNotFound e) {
            throw new IllegalArgumentException("The HLA interaction class <" + name + "> is not defined in any FOMs currently being used in this federation execution.");
        }

        return new HLAInteractionClass.Builder()
                .withName(name)
                .withMetadata(proxyMetadata)
                .withHandle(handle)
                .build();
    }

    public void publishInteractionClass(String name, Class<?> proxyClass) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress {
        if (name == null) {
            throw new IllegalArgumentException("The name of the HLA interaction class to be published must not be null.");
        }

        HLAInteractionClass interactionClass;
        if ((interactionClass = getInteractionClass(name)) == null) {
            interactionClass = createInteractionClass(name, proxyClass);
            this.interactionClasses.add(interactionClass);
        }

        proxyClassEqualityCheck(interactionClass.getProxyClass(), proxyClass);
        interactionClass.publish();
    }

    public void unpublishInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (name == null) {
            throw new IllegalArgumentException("The name of the HLA interaction class to be unpublished must not be null.");
        }

        HLAInteractionClass interactionClass;
        if ((interactionClass = getInteractionClass(name)) != null) {
            interactionClass.unpublish();
        }
    }

    public void subscribeInteractionClass(String name, Class<?> proxyClass) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress {
        if (name == null) {
            throw new IllegalArgumentException("The name of the HLA interaction class to be subscribed must not be null.");
        }

        HLAInteractionClass interactionClass;
        if ((interactionClass = getInteractionClass(name)) == null) {
            interactionClass = createInteractionClass(name, proxyClass);
            this.interactionClasses.add(interactionClass);
        }

        proxyClassEqualityCheck(interactionClass.getProxyClass(), proxyClass);
        interactionClass.subscribe();
    }

    public void unsubscribeInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (name == null) {
            throw new IllegalArgumentException("The name of the HLA interaction class to be unsubscribed must not be null.");
        }

        HLAInteractionClass interactionClass;
        if ((interactionClass = getInteractionClass(name)) != null) {
            interactionClass.unsubscribe();
        }
    }

    private void proxyClassEqualityCheck(Class<?> expected, Class<?> actual) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException("The HLA interaction class already uses <" + expected + "> to represent interaction objects and this cannot be reassigned to <" + actual + ">.");
        }
    }

    public void sendInteraction(Object proxy) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (proxy == null) {
            throw new IllegalArgumentException("The object representing the interaction to be sent must not be null.");
        }

        Class<?> proxyClass = proxy.getClass();
        HLAInteractionClass interactionClass = getInteractionClass(proxyClass);
        if (interactionClass == null) {
            throw new IllegalArgumentException("No HLA interaction class has been assigned the class <" + proxyClass.getName() + "> for serializing data.");
        } else {
            interactionClass.send(proxy);
        }
    }

    public void interactionReceived(InteractionClassHandle handle, ParameterHandleValueMap parameterValues, String producingFederateName) {
        HLAInteractionClass interactionClass = getInteractionClass(handle);

        if (interactionClass != null) {
            Object proxy = interactionClass.receive(parameterValues);
            this.interactionListeners.forEach(listener -> listener.received(proxy, producingFederateName));
        } else {
            logger.warn("Missed interaction from <{}> as no interaction class is known internally by the federate to deserialize the values received.", producingFederateName);
        }
    }

    public void addInteractionListener(InteractionListener listener) {
        if (listener != null) {
            this.interactionListeners.add(listener);
        }
    }

    public void removeInteractionListener(InteractionListener listener) {
        if (listener != null) {
            this.interactionListeners.remove(listener);
        }
    }
}
