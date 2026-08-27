/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java framework for developing
 SRFOM-compliant HLA Federates in the Simulation Exploration
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

package org.see.skf.internal.callbacks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

final class BiCallbackImpl<T,U> implements FederateBiCallback<T,U> {
    private final T target;
    private U outcome;
    private final FutureTask<U> task;

    private final CountDownLatch latch;

    BiCallbackImpl(T target, int latchCount) {
        this.target = target;
        this.task = createTask();

        this.latch = new CountDownLatch(latchCount);
    }

    private FutureTask<U> createTask() {
        return new FutureTask<>(() -> {
            this.latch.await();
            return this.outcome;
        });
    }

    @Override
    public T getTarget() {
        return this.target;
    }

    @Override
    public synchronized void complete(U outcome) {
        this.outcome = outcome;
        this.latch.countDown();
    }

    @Override
    public FutureTask<U> getTask() {
        return this.task;
    }
}
