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

package org.see.skf.runtime;

import hla.rti1516_2025.encoding.EncoderFactory;
import org.see.skf.core.SKUtilityFactory;
import org.see.skf.encoding.Coder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class CoderManager {

    // Each generic coder interface is of the form "Coder<some_random_type_here>" (potentially with extra characters surrounding it on either side).
    private static final String CODER_GENERIC_INTERFACE_NAME_PATTERN = ".*\\bCoder<\\b.*";

    private final Map<Class<? extends Coder<?>>, Method[]> coderClassToMethods;

    // Association of a class that extends the Coder interface to its serialization methods as a map.
    // Method[0] = encode() and Method[1] = decode()
    private final Map<Class<? extends Coder<?>>, Coder<?>> coderInstancePool;

    public CoderManager() {
        this.coderInstancePool = new HashMap<>();
        this.coderClassToMethods = new HashMap<>();
    }

    CoderReflectionData get(Class<? extends Coder<?>> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Can't fetch coder instance for NULL.");
        }

        Coder<?> coder = this.coderInstancePool.containsKey(clazz) ? this.coderInstancePool.get(clazz) : getCoder(clazz);
        Method[] coderMethods = this.coderClassToMethods.get(clazz);

        return new CoderReflectionData(coder, coderMethods[0], coderMethods[1]);
    }

    private Coder<?> getCoder(Class<? extends Coder<?>> clazz) {
        Class<?>[] ancestorWithGenerics = getAccessibleAncestorWithGenerics(clazz);
        Class<?> coderClass = ancestorWithGenerics[0];
        Class<?> genericType = ancestorWithGenerics[1];
        Method[] methods = getCoderMethods(coderClass, genericType);
        this.coderClassToMethods.put(clazz, methods);

        Coder<?> coder = instantiate(clazz);
        this.coderInstancePool.put(clazz, coder);

        return coder;
    }

    private Class<?>[] getAccessibleAncestorWithGenerics(Class<?> clazz) {
        Class<?> baseCoderType = null;
        Class<?> genericType = null;

        while (genericType == null) {
            Type[] genericInterfaces = clazz.getGenericInterfaces();

            for (Type genericInterface : genericInterfaces) {
                String typeName = genericInterface.getTypeName();

                if (typeName.matches(CODER_GENERIC_INTERFACE_NAME_PATTERN)) {
                    int startIndex = typeName.indexOf('<');
                    int endIndex = typeName.indexOf('>');
                    String coderGenericTypeName = typeName.substring(startIndex + 1, endIndex);

                    try {
                        genericType = Class.forName(coderGenericTypeName);
                        baseCoderType = clazz;
                        break;
                    } catch (ClassNotFoundException e) {
                        throw new CoderInstantiationException("", e);
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        // The baseCoderType is the original parent in the hierarchy directly descended from Coder<T> whose encode and decode methods that are accessible via Reflection.
        return new Class<?>[]{ baseCoderType, genericType };
    }

    private Method[] getCoderMethods(Class<?> clazz, Class<?> genericType) {
        try {
            Method encodeMethod = clazz.getDeclaredMethod("encode", genericType);
            Method decodeMethod = clazz.getDeclaredMethod("decode", byte[].class);

            return new Method[] { encodeMethod, decodeMethod };
        } catch (NoSuchMethodException e) {
            throw new CoderInstantiationException("Serialization methods are missing for the coder type <" + clazz.getName() + "> .", e);
        }
    }

    private Coder<?> instantiate(Class<? extends Coder<?>> clazz) {
        EncoderFactory encoderFactory = SKUtilityFactory.INSTANCE.getEncoderFactory();

        try {
            return clazz.getDeclaredConstructor(EncoderFactory.class).newInstance(encoderFactory);
        }  catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new CoderInstantiationException("Failed to instantiate the coder <" + clazz.getName() +">. Ensure that the class is concrete and features a publicly accessible constructor that accepts only an EncoderFactory argument is present.");
        } catch (InvocationTargetException e) {
            throw new CoderInstantiationException("The constructor for the coder <" + clazz.getName() + "> threw an exception.", e);
        }
    }

    static final class CoderReflectionData {
        private final Coder<?> coder;

        private final Method encodeMethod;

        private final Method decodeMethod;

        private CoderReflectionData(Coder<?> coder, Method encodeMethod, Method decodeMethod) {
            this.coder = coder;
            this.encodeMethod = encodeMethod;
            this.decodeMethod = decodeMethod;
        }

        public Coder<?> getCoder() {
            return this.coder;
        }

        public Method getEncodeMethod() {
            return this.encodeMethod;
        }

        public Method getDecodeMethod() {
            return this.decodeMethod;
        }
    }
}
