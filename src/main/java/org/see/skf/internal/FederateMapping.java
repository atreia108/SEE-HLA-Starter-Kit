package org.see.skf.internal;

import hla.rti1516_2025.FederateHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.HLAUtilityFactory;

import java.util.HashMap;
import java.util.Map;

public final class FederateMapping {
    private final RTIambassador rtiAmbassador;

    private final Map<FederateHandle, String> handleToName;
    private final Map<String, FederateHandle> nameToHandle;

    public FederateMapping() {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.handleToName = new HashMap<>();
        this.nameToHandle = new HashMap<>();
    }

    public void add(FederateHandle handle) {
        String federateName = this.handleToName.computeIfAbsent(handle, this::getFederateNameFromRti);
        this.nameToHandle.put(federateName, handle);
    }

    private String getFederateNameFromRti(FederateHandle handle) {
        return this.handleToName.computeIfAbsent(handle, h -> {
            try {
                return this.rtiAmbassador.getFederateName(handle);
            } catch (InvalidFederateHandle | FederateHandleNotKnown | FederateNotExecutionMember | NotConnected |
                     RTIinternalError e) {
                throw new RuntimeException("Name of the federate using the handle <" + handle + "> could not be retrieved from the RTI.", e);
            }
        });
    }

    public FederateHandle getHandle(String name) {
        return this.nameToHandle.get(name);
    }

    public String getName(FederateHandle handle) {
        return this.handleToName.get(handle);
    }
}
