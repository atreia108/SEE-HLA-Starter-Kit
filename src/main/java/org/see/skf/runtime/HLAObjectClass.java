package org.see.skf.runtime;

import hla.rti1516_2025.ObjectClassHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.FederateNotExecutionMember;
import hla.rti1516_2025.exceptions.NameNotFound;
import hla.rti1516_2025.exceptions.NotConnected;
import hla.rti1516_2025.exceptions.RTIinternalError;
import org.see.skf.core.SKUtilityFactory;
import org.see.skf.core.annotations.ObjectClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class HLAObjectClass {
    private final Logger logger = LoggerFactory.getLogger(HLAObjectClass.class);

    private final RTIambassador rtiAmbassador;

    private final String name;
    private final ObjectClassHandle handle;

    private final Set<HLAObjectClassAttribute> attributes;

    // TODO - Accept an ObjectClass annotation and unpack the respective Attribute annotations to create attributes.
    // TODO - Also, invert the attribute-class relationship: Create attributes FIRST and then subsequently add them to the object class at instantiation.
    public HLAObjectClass(String name) {
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();

        try {
            this.handle = rtiAmbassador.getObjectClassHandle(name);
        } catch (NameNotFound e) {
            throw new RtiHandleRetrievalException("<" + name + "> is not a valid object class in the FOM for this federation execution. Re-check name element in the @ObjectClass annotation.");

        } catch (FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new RtiHandleRetrievalException(e);
        }

        this.name = name;
        this.attributes = new CopyOnWriteArraySet<>();
    }

    public void publish() {

    }

    public void unpublish() {

    }

    public void subscribe() {

    }

    public void unsubscribe() {

    }

    public String getName() {
        return this.name;
    }

    public ObjectClassHandle getHandle() {
        return this.handle;
    }

    public Set<HLAObjectClassAttribute> getAttributes() {
        return this.attributes;
    }
}
