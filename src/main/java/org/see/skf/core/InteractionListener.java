package org.see.skf.core;

public interface InteractionListener {
    void received(Object interaction, String sourceFederateName);
}
