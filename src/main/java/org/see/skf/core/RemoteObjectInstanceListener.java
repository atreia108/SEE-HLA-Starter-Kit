package org.see.skf.core;

public interface RemoteObjectInstanceListener {

    void discovered(String producingFederateName);

    void initialized(Object instance);

    void destroyed(String producingFederateName);

}
