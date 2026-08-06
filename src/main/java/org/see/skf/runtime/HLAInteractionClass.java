package org.see.skf.runtime;

import hla.rti1516_2025.InteractionClassHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.FederateNotExecutionMember;
import hla.rti1516_2025.exceptions.NameNotFound;
import hla.rti1516_2025.exceptions.NotConnected;
import hla.rti1516_2025.exceptions.RTIinternalError;
import org.see.skf.core.SKUtilityFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public final class HLAInteractionClass {

    private final String name;
    private final InteractionClassHandle handle;
    private final AtomicBoolean published;
    private final AtomicBoolean subscribed;

    public HLAInteractionClass(String name) {
        RTIambassador rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        try {
            this.handle = rtiAmbassador.getInteractionClassHandle(name);
        } catch (NameNotFound e) {
            throw new RtiHandleException("<" + name + "> is not a valid interaction class in the FOM for this federation execution. Re-check name element in the @InteractionClass annotation.");
        } catch (FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new RtiHandleException(e);
        }

        this.name = name;
        this.published = new AtomicBoolean(false);
        this.subscribed = new AtomicBoolean(false);
    }

    public String getName() {
        return this.name;
    }
}
