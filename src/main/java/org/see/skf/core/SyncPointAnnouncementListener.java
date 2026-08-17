package org.see.skf.core;

@FunctionalInterface
public interface SyncPointAnnouncementListener {
    void announced(String synchronizationPointLabel);
}
