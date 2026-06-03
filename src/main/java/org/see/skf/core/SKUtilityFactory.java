package org.see.skf.core;

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.RtiFactory;
import hla.rti1516_2025.RtiFactoryFactory;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.exceptions.RTIinternalError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum SKUtilityFactory {
    INSTANCE;

    private static final String FRAMEWORK_VERSION = "2.1.0";

    private final RTIambassador rtiAmbassador;
    private final EncoderFactory encoderFactory;

    SKUtilityFactory() {
        try {
            RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
            Logger logger = LoggerFactory.getLogger(SKUtilityFactory.class);
            rtiAmbassador = rtiFactory.getRtiAmbassador();
            encoderFactory = rtiFactory.getEncoderFactory();

            String rtiName = rtiFactory.rtiName();
            String rtiVersion = rtiFactory.rtiVersion();
            String hlaStandard = rtiAmbassador.getHLAversion();
            logger.info("SEE HLA Starter Kit Version {}. RTI: {} {}. HLA Standard: {}", FRAMEWORK_VERSION, rtiName, rtiVersion, hlaStandard);
        } catch (RTIinternalError e) {
            throw new FederateStartupFailureException("Failed to initialize one or more HLA utility objects for use by the federate.", e);
        }
    }

    RTIambassador getRTIambassador() {
        return rtiAmbassador;
    }

    EncoderFactory getEncoderFactory() {
        return encoderFactory;
    }
}
