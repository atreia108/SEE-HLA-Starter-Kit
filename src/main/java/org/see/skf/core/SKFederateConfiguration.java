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

import org.see.skf.exceptions.FederateConfigurationReadException;
import org.see.skf.exceptions.InvalidFederateConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SKFederateConfiguration {
    private static final String RTI_ADDRESS_PROPERTY = "rtiAddress";
    private static final String FEDERATION_NAME_PROPERTY = "federationName";
    private static final String FEDERATE_NAME_PROPERTY = "federateName";
    private static final String FEDERATE_TYPE_PROPERTY = "federateType";
    private static final String LOOKAHEAD_PROPERTY = "lookahead";
    private static final String FOM_DIRECTORY_PROPERTY = "fomDirectory";

    // TODO - When and if federate protocol support becomes viable:
    // private static final String FEDERATE_PROTOCOL_MODE = "federateProtocolMode";

    private final String rtiAddress;
    private final String federationName;
    private final String federateName;
    private final String federateType;
    private final long lookahead;

    private final String[] additionalFomModules;

    // TODO - When and if federate protocol support becomes viable:
    // private final boolean federateProtocolMode

    SKFederateConfiguration(File confFile) {
        Properties configProperties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(confFile)) {
            configProperties.load(inputStream);
        } catch (IOException e) {
            throw new FederateConfigurationReadException("Failed to start federate due to I/O problems when attempting to read the configuration file.", e);
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
            throw new InvalidFederateConfigurationException("Value for <lookahead> property is not an integer.");
        }

        // TODO - When and if federate protocol support becomes viable:
        // Boolean type property registration check.

        String fomDir = configProperties.getProperty(FOM_DIRECTORY_PROPERTY);
        this.additionalFomModules = loadFomModules(fomDir);
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
