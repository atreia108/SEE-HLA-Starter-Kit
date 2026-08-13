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

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class SKFederateConfigurationTest {
    private static final String TEST_RESOURCES_PATH = "src" + File.separator + "test" + File.separator + "resources" + File.separator;
    private static final File validConfigFile = new File(TEST_RESOURCES_PATH + "valid.conf");

    private static final String RTI_ADDRESS_PROPERTY = "rtiAddress = localhost:8989";
    private static final String FEDERATION_NAME_PROPERTY = "federationName = SEE 2027";
    private static final String FEDERATE_NAME_PROPERTY = "federateName = Spaceport";
    private static final String FEDERATE_TYPE_PROPERTY = "federateType = Behavior";
    private static final String LOOKAHEAD_PROPERTY = "lookahead = 1000000";
    private static final String MAX_THREADS_PROPERTY = "maxThreads = 16";
    private static final String FOM_DIRECTORY_PROPERTY = "fomDirectory = src/test/resources/foms";

    @Test
    void testValidConfigProperties() {
        FederatePropertyConfiguration validConfig = new FederatePropertyConfiguration(validConfigFile);

        assertEquals("localhost:8989", validConfig.rtiAddress());
        assertEquals("SEE 2027", validConfig.federationName());
        assertEquals("Spaceport", validConfig.federateName());
        assertEquals("Behavior", validConfig.federateType());
        assertEquals(1000000, validConfig.lookahead());
        assertEquals(16, validConfig.maxThreads());

        assertTrue(configFomModulesAreValid(validConfigFile));
    }

    private boolean configFomModulesAreValid(File configFile) {
        FederatePropertyConfiguration config = new FederatePropertyConfiguration(configFile);

        for (String moduleFilePath : config.additionalFomModules()) {
            if (moduleFilePath == null || !configFile.exists()) {
                return false;
            }
        }

        return true;
    }

    @Test
    void testInvalidConfigProperties() throws IOException {
        String tempFilePath = TEST_RESOURCES_PATH + "invalid.conf";
        File tempConfigFile = new File(tempFilePath);

        assertTrue(tempConfigFile.createNewFile());

        FileInputStream inputStream = new FileInputStream(validConfigFile);
        BufferedReader reader = setupReader(inputStream);
        BufferedWriter writer = overwriteAndGet();

        copyAndRemoveLine(RTI_ADDRESS_PROPERTY, reader, writer, inputStream);
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(tempConfigFile));
        writeLine(writer, RTI_ADDRESS_PROPERTY);
        assertDoesNotThrow(() -> new FederatePropertyConfiguration(tempConfigFile));

        copyAndRemoveLine(FEDERATION_NAME_PROPERTY, reader, writer, inputStream);
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(tempConfigFile));
        writeLine(writer, FEDERATION_NAME_PROPERTY);
        assertDoesNotThrow(() -> new FederatePropertyConfiguration(tempConfigFile));

        copyAndRemoveLine(FEDERATE_NAME_PROPERTY, reader, writer, inputStream);
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(tempConfigFile));
        writeLine(writer, FEDERATE_NAME_PROPERTY);
        assertDoesNotThrow(() -> new FederatePropertyConfiguration(tempConfigFile));

        copyAndRemoveLine(FEDERATE_TYPE_PROPERTY, reader, writer, inputStream);
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(tempConfigFile));
        writeLine(writer, FEDERATE_TYPE_PROPERTY);
        assertDoesNotThrow(() -> new FederatePropertyConfiguration(tempConfigFile));

        copyAndRemoveLine(LOOKAHEAD_PROPERTY, reader, writer, inputStream);
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(tempConfigFile));
        writeLine(writer, LOOKAHEAD_PROPERTY);
        assertDoesNotThrow(() -> new FederatePropertyConfiguration(tempConfigFile));

        copyAndRemoveLine(MAX_THREADS_PROPERTY, reader, writer, inputStream);
        assertDoesNotThrow(() -> new FederatePropertyConfiguration(tempConfigFile));
        writeLine(writer, MAX_THREADS_PROPERTY);
        assertDoesNotThrow(() -> new FederatePropertyConfiguration(tempConfigFile));

        copyAndRemoveLine(FOM_DIRECTORY_PROPERTY, reader, writer, inputStream);
        assertEquals(0, new FederatePropertyConfiguration(tempConfigFile).additionalFomModules().length);
        writeLine(writer, FOM_DIRECTORY_PROPERTY);
        assertTrue(new FederatePropertyConfiguration(tempConfigFile).additionalFomModules().length > 0);

        assertTrue(configFomModulesAreValid(tempConfigFile));

        writer.close();
        reader.close();
        Files.delete(tempConfigFile.toPath());
    }

    private void copyAndRemoveLine(String targetLine, BufferedReader reader, BufferedWriter writer, FileInputStream inputStream) throws IOException {
        reader = setupReader(inputStream);
        writer = overwriteAndGet();

        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            currentLine = currentLine.trim();
            if (currentLine.equals(targetLine)) {
                continue;
            }

            writer.write(currentLine + System.lineSeparator());
        }

        writer.flush();
    }

    private void writeLine(BufferedWriter writer, String targetLine) throws IOException {
        writer = appendAndGet();

        writer.write(targetLine + System.lineSeparator());
        writer.flush();
    }

    private BufferedReader setupReader(FileInputStream inStream) throws IOException {
        inStream.getChannel().position(0);
        return new BufferedReader(new InputStreamReader(inStream));
    }

    private BufferedWriter overwriteAndGet() throws IOException {
        Path pathObject = Paths.get(TEST_RESOURCES_PATH + "invalid.conf");
        return Files.newBufferedWriter(pathObject, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private BufferedWriter appendAndGet() throws IOException {
        Path pathObject = Paths.get(TEST_RESOURCES_PATH + "invalid.conf");
        return Files.newBufferedWriter(pathObject, StandardOpenOption.APPEND);
    }
}