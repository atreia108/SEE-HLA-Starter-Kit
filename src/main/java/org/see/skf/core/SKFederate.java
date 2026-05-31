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

    private final SKFederateConfiguration config;
    private final RtiConfiguration rtiConfiguration;
    private final SKFederateAmbassador federateAmbassador;

    private final RTIambassador rtiAmbassador;

    protected SKFederate(File configurationFile) {
        this.config = new SKFederateConfiguration(configurationFile);
        this.rtiConfiguration = RtiConfiguration.createConfiguration()
                .withRtiAddress(config.rtiAddress());

        this.federateAmbassador = new SKFederateAmbassador();
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRTIambassador();
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
            throw new FederateStartupFailedException("Failed to establish connection to the RTI hosted at <" + rtiAddress + ">.", e);
        }
    }

    /**
     * TODO
     *
     */
    public final void joinFederationExecution() {
        String federateName = config.federateName();
        String federateType = config.federateType();
        String federationName = config.federationName();
        String[] fomModules = config.additionalFomModules();

        boolean joined = false;
        String federateNameSuffix = "";
        int attempts = 1;

        // TODO - Consider if an upper bound should be placed on the number of attempts the federate will entertain for joining a federation execution.
        while (!joined) {
            try {
                if (fomModules.length > 0) {
                    rtiAmbassador.joinFederationExecution(federateName + federateNameSuffix, federateType, federationName, fomModules);
                } else {
                    rtiAmbassador.joinFederationExecution(federateName + federateNameSuffix, federateType, federationName);
                }

                joined = true;

                if (attempts > 1) {
                    logger.warn("The name <{}> is already taken by another federate. Adopting <{}{}> as name instead.", config.federateName(), federateName, federateNameSuffix);
                }

                logger.info("Joined the federation execution <{}>.", federationName);
            } catch (FederateNameAlreadyInUse e) {
                // Attempt to join again with an incremented suffix at the end of the federate name.
                federateNameSuffix = "_" + attempts++;
            } catch (FederationExecutionDoesNotExist e) {
                throw new FederateStartupFailedException("The federation execution <" + federationName + "> does not exist.", e);
            } catch (InvalidFOM | ErrorReadingFOM | CouldNotOpenFOM | InconsistentFOM e) {
                throw new FederateStartupFailedException("Failed to join the federation execution <" + federationName + "> due to problems with parsing the supplied FOM modules.", e);
            } catch (FederateAlreadyExecutionMember e) {
                // IGNORE: Since the federate is already a part of the federation execution, there's no actual problem here.
            } catch (CouldNotCreateLogicalTimeFactory | SaveInProgress | RestoreInProgress | Unauthorized |
                     NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
                throw new FederateStartupFailedException("Failed to join the federation execution <" + federationName + ">.", e);
            }
        }
    }

    public final void resignFromFederationExecution() throws FederateShutdownAbortedException {
        try {
            rtiAmbassador.resignFederationExecution(ResignAction.DELETE_OBJECTS_THEN_DIVEST);
        } catch (OwnershipAcquisitionPending | FederateOwnsAttributes e) {
            throw new FederateShutdownAbortedException("Attempt to shut down federate was aborted due to ongoing processes that are yet unfulfilled.", e);
        } catch (FederateNotExecutionMember | NotConnected | CallNotAllowedFromWithinCallback | InvalidResignAction |
                 RTIinternalError e) {
            throw new FederateShutdownFailedException("Failed to shut down federate.", e);
        }
    }

    RTIambassador getRTIAmbassador() {
        return rtiAmbassador;
    }
}
