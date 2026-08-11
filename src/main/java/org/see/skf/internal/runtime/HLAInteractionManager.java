package org.see.skf.internal.runtime;

import hla.rti1516_2025.RTIambassador;
import org.see.skf.core.HLAUtilityFactory;
import org.see.skf.core.InteractionListener;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

public final class HLAInteractionManager {

    private final RTIambassador rtiAmbassador;

    private final SKAnnotatedTypeParser parser;
    private final ExecutorService executor;

    private final Set<HLAInteractionClass> interactionClasses;
    private final Set<InteractionListener> interactionListeners;

    public HLAInteractionManager(SKAnnotatedTypeParser parser, ExecutorService executor) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.parser = parser;
        this.executor = executor;

        this.interactionClasses = new CopyOnWriteArraySet<>();
        this.interactionListeners = new CopyOnWriteArraySet<>();
    }
}
