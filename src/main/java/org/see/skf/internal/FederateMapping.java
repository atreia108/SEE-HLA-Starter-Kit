package org.see.skf.internal;

import hla.rti1516_2025.FederateHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class FederateMapping {

    private static final Logger logger = LoggerFactory.getLogger(FederateMapping.class);

    private final RTIambassador rtiAmbassador;

    private final Map<FederateHandle, String> handleToName;

    public FederateMapping() {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.handleToName = new HashMap<>();
    }

    public String get(FederateHandle handle) {
        this.handleToName.computeIfAbsent(handle, h -> {
            try {
                return this.rtiAmbassador.getFederateName(handle);
            } catch (InvalidFederateHandle | FederateHandleNotKnown | FederateNotExecutionMember | NotConnected |
                     RTIinternalError e) {
                logger.error("Name of the federate using the handle <{}> could not be retrieved from the RTI.", handle, e);
                return null;
            }
        });

        return this.handleToName.get(handle);
    }
}
