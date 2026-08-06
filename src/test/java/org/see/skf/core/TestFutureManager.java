package org.see.skf.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

class TestFutureManager {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final Random rng;

    private final Map<String, FutureTask<Boolean>> pendingNameReservations;

    private final Map<String, Boolean> nameReservationOutcomes;
    private final Set<NameReservationCallback> nameReservationCallbacks;

    private final List<String> names;

    public TestFutureManager() {
        this.rng = new Random();
        this.pendingNameReservations = new ConcurrentHashMap<>();
        this.nameReservationOutcomes = new ConcurrentHashMap<>();
        this.nameReservationCallbacks = new CopyOnWriteArraySet<>();

        this.names = List.of(
                "brunel_lander",
                "brunel_spaceport",
                "brunel_spaceport_arm",
                "facens_rover"
        );
    }

    public Future<Boolean> simulateCallToRti() {
        String objectName = pickRandomName();
        Supplier<Boolean> method = () -> this.nameReservationOutcomes.get(objectName);
        NameReservationCallback callback = new NameReservationCallback(objectName, method);

        this.nameReservationCallbacks.add(callback);
        FutureTask<Boolean> future = callback.getFuture();
        executor.submit(future);
        System.out.println("Filed name reservation request for <" + objectName + ">.");

        return future;

        /*
        String name = pickRandomName();
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            while (true) {
                Boolean outcome = this.nameReservationOutcomes.get(name);
                // System.out.println(outcome);
                if (outcome != null) {
                    return outcome;
                }
            }
        });

        System.out.println("Reserving name <" + name + ">.");
        this.pendingNameReservations.put(name, task);
        executor.submit(task);

        return task;
         */
    }

    public void simulateCallback() {
        for (NameReservationCallback callback : this.nameReservationCallbacks) {
            Boolean outcome = rng.nextBoolean();

            this.nameReservationOutcomes.put(callback.getName(), outcome);
            this.nameReservationCallbacks.remove(callback);
        }

        /*
        for (String pendingName :  pendingNameReservations.keySet()) {
            Boolean outcome = rng.nextBoolean();
            this.nameReservationOutcomes.put(pendingName, outcome);
            this.pendingNameReservations.remove(pendingName);

            // System.out.println("Name reservation callback done for: " + pendingName + " OUTCOME: " + outcome);
        }
         */
    }

    public String pickRandomName() {
        int num = rng.nextInt(names.size());
        return names.get(num);
    }

    public static void main(String[] args) {
        TestFutureManager manager = new TestFutureManager();

        new Thread(() -> {
            while (true) {
                try {
                    Future<Boolean> future = manager.simulateCallToRti();
                    boolean result = future.get();

                    System.out.println("Name reservation callback complete! OUTCOME: " + result);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000L);
                    manager.simulateCallback();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}

class NameReservationCallback {
    private final String name;
    private final FutureTask<Boolean> future;
    private final Supplier<Boolean> outcome;

    NameReservationCallback(String name, Supplier<Boolean> outcome) {
        this.name = name;
        this.outcome = outcome;
        this.future = createFuture();
    }

    private FutureTask<Boolean> createFuture() {
        return new FutureTask<>(() -> {
            while (true) {
                Boolean result = this.outcome.get();

                if (result != null) {
                    System.out.println("Name reservation for <" + this.name + " completed. OUTCOME: " + result);
                    return result;
                }
            }
        });
    }

    public String getName() {
        return this.name;
    }

    public FutureTask<Boolean> getFuture() {
        return this.future;
    }
}
