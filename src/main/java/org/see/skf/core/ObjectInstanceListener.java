package org.see.skf.core;

public interface ObjectInstanceListener {

    void created(String name, Object instance, String producingFederateName);

    void destroyed(String name, String producingFederateName);
}
