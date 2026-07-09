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
import org.see.skf.runtime.HLAClassManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

public abstract class SKFederate {
    private static final Logger logger = LoggerFactory.getLogger(SKFederate.class);

    private final RtiConfiguration rtiConfiguration;
    private final RTIambassador rtiAmbassador;

    private String federateName;
    private final String federateType;
    private final String federationName;
    private final String[] additionalFomModules;
    private final SKFederateAmbassador federateAmbassador;

    private final HLAClassManager classManager;
    // private final SimulationTime simTime;

    protected SKFederate(File configurationFile) {
        SKFederateConfiguration config = new SKFederateConfiguration(configurationFile);
        this.federateName = config.federateName();
        this.federateType = config.federateType();
        this.federationName = config.federationName();
        this.additionalFomModules = config.additionalFomModules();

        this.rtiConfiguration
                = RtiConfiguration.createConfiguration()
                .withRtiAddress(config.rtiAddress());
        this.rtiAmbassador = SKUtilityFactory.INSTANCE.getRtiAmbassador();
        this.federateAmbassador = new SKFederateAmbassador();
        this.classManager = new HLAClassManager();

        connectToRTI();
        joinFederationExecution();

        // Lazy initialization of simTime with just the lookAhead parameter. Federation-specific values will follow later
        // once the values of the ExCO object instance are retrieved.
        // long lookAhead = config.lookahead();
        // this.simTime = new SimulationTime(lookAhead);

        configureAndStart();
    }

    /**
     * TODO
     *
     */
    private void connectToRTI() {
        String rtiAddress = rtiConfiguration.rtiAddress();

        try {
            rtiAmbassador.connect(federateAmbassador, CallbackModel.HLA_IMMEDIATE, rtiConfiguration);
            logger.info("Established connection to RTI hosted at <{}>.", rtiAddress);
        } catch (AlreadyConnected ignore) {
            logger.warn("<{}> is already connected to the RTI hosted at <{}>.", federateName, rtiAddress);
        }
        catch (Unauthorized | ConnectionFailed | UnsupportedCallbackModel | CallNotAllowedFromWithinCallback |
                 RTIinternalError e) {
            throw new FederateStartupException("Failed to establish connection to the RTI hosted at <" + rtiAddress + ">.", e);
        }
    }

    /**
     * TODO
     *
     */
    private void joinFederationExecution() {
        String originalFederateName = federateName;
        String suffix = "";
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
                federateName = originalFederateName + suffix + "_" + attempts++;
            } catch(FederateAlreadyExecutionMember ignore) {
                logger.warn("<{}> is already a member of the federation execution <{}>.", federateName, federationName);
            } catch (FederationExecutionDoesNotExist e) {
                throw new FederateStartupException("The federation execution <" + federationName + "> does not exist.", e);
            } catch (InvalidFOM | ErrorReadingFOM | CouldNotOpenFOM | InconsistentFOM e) {
                throw new FederateStartupException("Failed to join the federation execution <" + federationName + "> due to problems with parsing the supplied FOM modules.", e);
            } catch (CouldNotCreateLogicalTimeFactory | SaveInProgress | RestoreInProgress | Unauthorized |
                     NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
                throw new FederateStartupException("Failed to join the federation execution <" + federationName + ">.", e);
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

    public final void shutdownExecution() throws FederateShutdownInterruptedException {
        try {
            rtiAmbassador.resignFederationExecution(ResignAction.DELETE_OBJECTS_THEN_DIVEST);
        } catch (OwnershipAcquisitionPending | FederateOwnsAttributes e) {
            throw new FederateShutdownInterruptedException("Federate shutdown attempt was interrupted by ongoing processes that are yet to be completed.", e);
        } catch (FederateNotExecutionMember | NotConnected | CallNotAllowedFromWithinCallback | InvalidResignAction |
                 RTIinternalError e) {
            throw new FederateShutdownFailedException("Failed to shutdown federate.", e);
        }
    }

    public final void publishObjectClass(String className, String... attributes) {
        // TODO
    }

    public final void unpublishObjectClass(String className, String... attributes) {
        // TODO
    }

    public final void subscribeObjectClass(String className, String... attributes) {
        // TODO
    }

    public final void unsubscribeObjectClass(String className, String... attributes) {
        // TODO
    }

    public final void createObjectInstance(Object objectInstance) {
        // TODO
    }

    public final void updateObjectInstance(Object objectInstance, String... attributes) {
        // TODO
    }

    public final void destroyObjectInstance(Object objectInstance) {
        // TODO
    }

    public final void setupTimeManagement() {
        // TODO - Enable time regulation and constraint for messages, then compute and advance to HLTB.
        // simTime.regulateTime();
        // simTime.constrainTime();
    }

    // TODO - Object instance creation w/o name.
    public void createObjectInstance() {

    }

    // TODO - Object instance creation w/ name.
    public void createObjectInstance(String name) {

    }

    // TODO - Object instance deletion.
    public void destroyObjectInstance() {

    }

    public abstract void configureAndStart();

    protected abstract void update();

    public final String getName() {
        return federateName;
    }

    public final String getType() {
        return federateType;
    }

    // TODO - Returns the latest sim time in TJD.
    public final synchronized double getSimulationTime() {
        // return simTime.getTJDTime();
        return 0.0;
    }
}
