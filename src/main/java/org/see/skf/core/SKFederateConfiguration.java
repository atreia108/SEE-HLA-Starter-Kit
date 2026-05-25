/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java library that supports
 the development of HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London. All rights reserved.

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class SKFederateConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SKFederateConfiguration.class);

    private static final String RTI_ADDRESS_PROPERTY = "rtiAddress";
    private static final String FEDERATION_NAME_PROPERTY = "federationName";
    private static final String FEDERATE_NAME_PROPERTY = "federateName";
    private static final String FEDERATE_TYPE_PROPERTY = "federateType";
    private static final String LOOKAHEAD_PROPERTY = "lookahead";
    private static final String FOM_DIRECTORY_PROPERTY = "fomDirectory";

    private final String rtiAddress;
    private final String federationName;
    private final String federateName;
    private final String federateType;
    private final long lookahead;

    private final String[] additionalFomModules;

    SKFederateConfiguration(File confFile) {
        Properties configProperties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(confFile)) {
            configProperties.load(inputStream);
        } catch (IOException e) {
            throw new FederateConfigurationReadException("Failed to start federate due to I/O problems while attempting to read the configuration file.", e);
        }

        this.rtiAddress = configProperties.getProperty(RTI_ADDRESS_PROPERTY);
        this.federationName = configProperties.getProperty(FEDERATION_NAME_PROPERTY);
        this.federateName = configProperties.getProperty(FEDERATE_NAME_PROPERTY);
        this.federateType = configProperties.getProperty(FEDERATE_TYPE_PROPERTY);

        validateStringTypeProperties();

        // No further validations required for lookahead and (potentially) federateProtocolMode properties since the
        // following effectively guarantees that they are non-null.
        try {
            this.lookahead = Long.parseLong(configProperties.getProperty(LOOKAHEAD_PROPERTY));
        } catch (NumberFormatException e) {
            throw new InvalidFederateConfigurationException("Value for <lookahead> property is not an integer.", e);
        }

        String fomDir = configProperties.getProperty(FOM_DIRECTORY_PROPERTY);
        this.additionalFomModules = loadFomModules(fomDir);

        // TODO - Remove after v2.1 release
        // The checks performed in the method below is purely intended to inform users that certain properties carried
        // over from v1 have been deprecated and their presence in the config file is redundant. It is anticipated that
        // users will follow these instructions and there won't be a point in keeping this around post-v2.1.
        warnDeprecatedProperties(configProperties);

        logger.debug("Finished loading configuration properties for the federate <{}>.", federateName);
    }

    private String[] loadFomModules(String path) {
        if (path != null) {
            File directory = new File(path);

            if (directory.exists() && directory.isDirectory()) {
                File[] fomModules = directory.listFiles();

                if (fomModules != null) {
                    List<String> pathList = new ArrayList<>();
                    for (File module : fomModules) {
                        pathList.add(module.getAbsolutePath());
                    }

                    return pathList.toArray(new String[0]);
                }
            }
        }

        return new String[0];
    }

    private void validateStringTypeProperties() {
        if (rtiAddress == null) {
            throw new InvalidFederateConfigurationException("No value supplied for <rtiAddress> property.");
        }

        if (federationName == null) {
            throw new InvalidFederateConfigurationException("No value supplied for <federationName> property.");
        }

        if (federateName == null) {
            throw new InvalidFederateConfigurationException("No value supplied for <federateName> property.");
        }

        if (federateType == null) {
            throw new InvalidFederateConfigurationException("No value supplied for <federateType> property.");
        }
    }

    private void warnDeprecatedProperties(Properties properties) {
        final String DEPRECATED_FEDERATE_ROLE_PROPERTY = properties.getProperty("federateRole");
        final String DEPRECATED_ASYNC_DELIVERY_PROPERTY = properties.getProperty("asynchronousDelivery");
        final String DEPRECATED_TIME_REGULATING_PROPERTY = properties.getProperty("timeRegulating");
        final String DEPRECATED_TIME_CONSTRAINED_PROPERTY = properties.getProperty("timeConstrained");

        final String[] deprecatedProperties = new String[] {
                DEPRECATED_FEDERATE_ROLE_PROPERTY,
                DEPRECATED_ASYNC_DELIVERY_PROPERTY,
                DEPRECATED_TIME_REGULATING_PROPERTY,
                DEPRECATED_TIME_CONSTRAINED_PROPERTY
        };

        for (String property : deprecatedProperties) {
            if (property != null) {
                logger.warn("Remove useless property definition <{}> that was deprecated in v2.1 from configuration file.", property);
            }
        }
    }

    public String rtiAddress() {
        return rtiAddress;
    }

    public String federationName() {
        return federationName;
    }

    public String federateName() {
        return federateName;
    }

    public String federateType() {
        return federateType;
    }

    public long lookahead() {
        return lookahead;
    }

    public String[] additionalFomModules() {
        return additionalFomModules;
    }
}
