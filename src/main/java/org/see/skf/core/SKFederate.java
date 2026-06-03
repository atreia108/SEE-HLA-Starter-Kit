/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java library that supports
 the development of HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London (UK). All rights reserved.

 GNU Lesser General Public License (GNU LGPL).

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3.0 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library.
 If not, see http://http://www.gnu.org/licenses/
 *****************************************************************/

package org.see.skf.core;

import hla.rti1516_2025.CallbackModel;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.ResignAction;
import hla.rti1516_2025.RtiConfiguration;
import hla.rti1516_2025.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class SKFederate {
    private static final Logger logger = LoggerFactory.getLogger(SKFederate.class);

    private final RtiConfiguration rtiConfiguration;
    private final RTIambassador rtiAmbassador;

    private String federateName;
    private final String federateType;
    private final String federationName;
    private final String[] additionalFomModules;
    private final SKFederateAmbassador federateAmbassador;

    protected SKFederate(File configurationFile) {
        SKFederateConfiguration config = new SKFederateConfiguration(configurationFile);
        federateName = config.federateName();
        federateType = config.federateType();
        federationName = config.federationName();
        additionalFomModules = config.additionalFomModules();

        this.rtiConfiguration = RtiConfiguration.createConfiguration()
                .withRtiAddress(config.rtiAddress());
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRTIambassador();
        this.federateAmbassador = new SKFederateAmbassador();
    }

    /**
     * TODO
     *
     */
    public final void connectToRTI() {
        String rtiAddress = rtiConfiguration.rtiAddress();

        try {
            rtiAmbassador.connect(federateAmbassador, CallbackModel.HLA_IMMEDIATE, rtiConfiguration);
            logger.info("Established connection to RTI hosted at <{}>.", rtiAddress);
        } catch (AlreadyConnected ignore) {
            // No exception needs to be thrown for this since it does not qualify as an "un-ideal" scenario.
        } catch (Unauthorized | ConnectionFailed | UnsupportedCallbackModel | CallNotAllowedFromWithinCallback |
                 RTIinternalError e) {
            throw new FederateStartupFailureException("Failed to establish connection to the RTI hosted at <" + rtiAddress + ">.", e);
        }
    }

    /**
     * TODO
     *
     */
    public final void joinFederationExecution() {
        String originalFederateName = federateName;
        String federateNameSuffix = "";
        boolean joined = false;
        int attempts = 1;

        // TODO - Consider if an upper bound should be placed on the number of attempts the federate will entertain for joining a federation execution.
        while (!joined) {
            try {
                attemptJoin();

                if (attempts > 1) {
                    logger.warn("The name <{}> was already taken by another federate. Assuming the name <{}> instead.", originalFederateName, federateName);
                }

                logger.info("Joined the federation execution <{}>.", federationName);
                joined = true;
            } catch (FederateNameAlreadyInUse e) {
                // Attempt to join again with an incremented suffix at the end of the federate name.
                federateName = originalFederateName + federateNameSuffix + "_" + attempts++;
            } catch (FederationExecutionDoesNotExist e) {
                throw new FederateStartupFailureException("The federation execution <" + federationName + "> does not exist.", e);
            } catch (InvalidFOM | ErrorReadingFOM | CouldNotOpenFOM | InconsistentFOM e) {
                throw new FederateStartupFailureException("Failed to join the federation execution <" + federationName + "> due to problems with parsing the supplied FOM modules.", e);
            } catch (FederateAlreadyExecutionMember e) {
                logger.warn("<{}> is already a member of the federation execution <{}>.", federateName, federationName);
            } catch (CouldNotCreateLogicalTimeFactory | SaveInProgress | RestoreInProgress | Unauthorized |
                     NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
                throw new FederateStartupFailureException("Failed to join the federation execution <" + federationName + ">.", e);
            }
        }
    }

    private void attemptJoin() throws CouldNotOpenFOM, NotConnected, InvalidFOM, RTIinternalError, ErrorReadingFOM, CouldNotCreateLogicalTimeFactory, FederateNameAlreadyInUse, RestoreInProgress, CallNotAllowedFromWithinCallback, InconsistentFOM, FederationExecutionDoesNotExist, Unauthorized, FederateAlreadyExecutionMember, SaveInProgress {
        if (additionalFomModules.length > 0) {
            rtiAmbassador.joinFederationExecution(federateName, federateType, federationName, additionalFomModules);
        } else {
            rtiAmbassador.joinFederationExecution(federateName, federateType, federationName);
        }
    }

    public final void shutdown() throws FederateShutdownAbortedException {
        try {
            rtiAmbassador.resignFederationExecution(ResignAction.DELETE_OBJECTS_THEN_DIVEST);
        } catch (OwnershipAcquisitionPending | FederateOwnsAttributes e) {
            throw new FederateShutdownAbortedException("Federate shutdown attempt was aborted due to ongoing processes that are yet to be fulfilled.", e);
        } catch (FederateNotExecutionMember | NotConnected | CallNotAllowedFromWithinCallback | InvalidResignAction |
                 RTIinternalError e) {
            throw new FederateShutdownFailureException("Failed to shut down federate.", e);
        }
    }

    RTIambassador getRTIAmbassador() {
        return rtiAmbassador;
    }

    public String getName() {
        return federateName;
    }

    public String getDesignatedType() {
        return federateType;
    }
}
