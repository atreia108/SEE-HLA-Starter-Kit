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

package org.see.skf.core.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * A class bearing this annotation is treated as the representation of an HLA object class. A subclass automatically
 * inherits the parent's definition of the annotation along with attribute definitions.
 * </p>
 * <p>
 * If an attempt is made to re-declare an object class (i.e., publish/subscribe) with attributes that were not previously
 * specified in the {@code publishableAttributes} and {@code subscribableAttributes}, the federate will automatically declare those attributes to the RTI.
 * </p>
 *
 * @see Attribute
 * @see InteractionClass
 * @since 1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ObjectClass {
    /**
     * Name of the HLA object class as defined in the FOM.
     *
     * @return The HLA object class name.
     */
    String name();
}
