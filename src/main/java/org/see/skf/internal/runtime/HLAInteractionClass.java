package org.see.skf.internal.runtime;

import hla.rti1516_2025.InteractionClassHandle;
import hla.rti1516_2025.ParameterHandle;
import hla.rti1516_2025.ParameterHandleValueMap;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.internal.InternalObjectBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

final class HLAInteractionClass {

    private static final Logger logger = LoggerFactory.getLogger(HLAInteractionClass.class);

    private final RTIambassador rtiAmbassador;

    private final String name;

    private final InteractionClassHandle handle;

    private final Class<?> proxyClass;

    private final Map<Parameter, Trait> parameterToTrait;

    private final AtomicBoolean published;

    private final AtomicBoolean subscribed;

    HLAInteractionClass(Builder builder) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.name = builder.name;
        this.handle = builder.handle;
        this.proxyClass = builder.objectMetadata.getProxyClass();
        this.parameterToTrait = computeParameterToTraitAssociation(builder.objectMetadata.getTraits());

        this.published = new AtomicBoolean(false);
        this.subscribed = new AtomicBoolean(false);
    }

    private Map<Parameter, Trait> computeParameterToTraitAssociation(Set<Trait> traits) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        // N.B. A standard HashMap will suffice since no new write operations will occur again as parameters are FIXED.
        Map<Parameter, Trait> map = new HashMap<>();

        for (Trait trait : traits) {
            String parameterName = trait.getAnnotatedName();
            try {
                ParameterHandle parameterHandle = rtiAmbassador.getParameterHandle(this.handle, parameterName);
                Parameter parameter = new Parameter(parameterName, parameterHandle);

                map.put(parameter, trait);
            } catch (InvalidInteractionClassHandle e) {
                throw new RtiHandleAcquisitionException(e);
            } catch (NameNotFound e) {
                throw new RtiHandleAcquisitionException("The parameter <" + parameterName + "> is not defined for the HLA interaction class <" + this.name + ">.");
            }
        }

        return map;
    }

    void publish() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (published.get()) {
            logger.warn("Redundant attempt to publish the HLA interaction class <{}> that has been previously published already.", this.name);
            return;
        }

        try {
            rtiAmbassador.publishInteractionClass(this.handle);
        } catch (InteractionClassNotDefined e) {
            // Highly unlikely to occur as we've already taken care of this during initialization.
            throw new RuntimeException(e);
        }

        logger.info("Published HLA interaction class <{}>.", this.name);
        this.published.set(true);
    }

    void unpublish() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (!published.get()) {
            logger.warn("Redundant attempt to unpublish to HLA interaction class <{}> that has not been previously published.", this.name);
            return;
        }

        try {
            rtiAmbassador.unpublishInteractionClass(this.handle);
        } catch (InteractionClassNotDefined e) {
            throw new RuntimeException(e);
        }

        logger.info("Unpublished HLA interaction class <{}>.", this.name);
        this.published.set(false);
    }

    void subscribe() throws FederateNotExecutionMember, RestoreInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, RTIinternalError, SaveInProgress {
        if (subscribed.get()) {
            logger.warn("Redundant attempt to subscribe to HLA interaction class <{}> that has been previously subscribed already.", this.name);
            return;
        }

        try {
            rtiAmbassador.subscribeInteractionClass(this.handle);
        } catch (InteractionClassNotDefined e) {
            throw new RuntimeException(e);
        }

        logger.info("Subscribed HLA interaction class <{}>.", this.name);
        this.subscribed.set(true);
    }

    void unsubscribe() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (!subscribed.get()) {
            logger.warn("Redundant attempt to unsubscribe to HLA interaction class <{}> that has not been previously subscribed.", this.name);
            return;
        }

        try {
            rtiAmbassador.unsubscribeInteractionClass(this.handle);
        } catch (InteractionClassNotDefined e) {
            throw new RuntimeException(e);
        }

        logger.info("Unsubscribed HLA interaction class <{}>.", this.name);
        this.subscribed.set(false);
    }

    private ParameterHandleValueMap serialize(Object forObject) throws FederateNotExecutionMember, NotConnected {
        ParameterHandleValueMap map = rtiAmbassador.getParameterHandleValueMapFactory().create(this.parameterToTrait.size());

        for (Map.Entry<Parameter, Trait> entry : this.parameterToTrait.entrySet()) {
            Parameter parameter = entry.getKey();
            Trait trait = entry.getValue();

            map.put(parameter.handle, trait.encode(forObject));
        }

        return map;
    }

    void send(Object proxy) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress {
        ParameterHandleValueMap map = serialize(proxy);

        try {
            rtiAmbassador.sendInteraction(this.handle, map, null);
            logger.debug("Sent an interaction of class <{}>.", this.name);
        } catch (InteractionClassNotPublished | InteractionClassNotDefined e) {
            throw new RuntimeException(e);
        } catch (InteractionParameterNotDefined e) {
            throw new SendInteractionException("One or more undefined parameters exist for this model using the HLA interaction class <" + this.name + ">.");
        }
    }

    Object receive(ParameterHandleValueMap map) {
        Object proxy = createProxy();

        for (Map.Entry<ParameterHandle, byte[]> entry : map.entrySet()) {
            Parameter parameter = getParameter(entry.getKey());

            if (parameter != null) {
                Trait t = this.parameterToTrait.get(parameter);
                t.decode(proxy, entry.getValue());
            }
        }

        return proxy;
    }

    private Parameter getParameter(ParameterHandle parameterHandle) {
        return this.parameterToTrait.keySet()
                .stream()
                .filter(p -> p.getHandle().equals(parameterHandle))
                .findFirst()
                .orElse(null);
    }

    String getName() {
        return this.name;
    }

    InteractionClassHandle getHandle() {
        return this.handle;
    }

    Class<?> getProxyClass() {
        return this.proxyClass;
    }

    Object createProxy() {
        try {
            return this.proxyClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create object to deserialize incoming interaction data.", e);
        }
    }

    private static class Parameter {

        private final String name;

        private final ParameterHandle handle;

        Parameter(String name, ParameterHandle handle) {
            this.name = name;
            this.handle = handle;
        }

        public String getName() {
            return name;
        }

        public ParameterHandle getHandle() {
            return handle;
        }
    }

    static class Builder {

        private String name;

        private InteractionClassHandle handle;

        private SKAnnotatedTypeParser2.Metadata objectMetadata;

        Builder withName(String name) {
            this.name = name;
            return this;
        }

        Builder withHandle(InteractionClassHandle handle) {
            this.handle = handle;
            return this;
        }

        Builder withMetadata(SKAnnotatedTypeParser2.Metadata objectMetadata) {
            this.objectMetadata = objectMetadata;
            return this;
        }

        HLAInteractionClass build() throws FederateNotExecutionMember, NotConnected, RTIinternalError {
            if (this.name == null || this.handle == null || this.objectMetadata == null) {
                throw new InternalObjectBuilderException("Missing one or more arguments required to initialize internal framework object representing an HLA interaction class.");
            }

            if (!this.name.equals(this.objectMetadata.getFomClassName())) {
                throw new MetadataMismatchException("Name of the interaction class represented by <" + this.objectMetadata.getFomClassName() + "> does not match the HLA interaction class <" + this.name + "> that it is intended to represent.");
            }

            return new HLAInteractionClass(this);
        }
    }
}
