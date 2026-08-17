package org.see.skf.core;

@FunctionalInterface
public interface ObjectInstanceDestroyedListener {

    void destroyed(String name);

}
