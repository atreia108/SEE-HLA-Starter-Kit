package org.see.skf.core;

@FunctionalInterface
public interface FederationSynchronizedListener {
    void synced(String synchronizationPointLabel);
}
