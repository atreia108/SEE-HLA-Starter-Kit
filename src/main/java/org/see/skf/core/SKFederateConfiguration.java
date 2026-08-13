package org.see.skf.core;

public interface SKFederateConfiguration {

    String rtiAddress();

    String federationName();

    String federateName();

    String federateType();

    long lookahead();

    int maxThreads();

    String[] additionalFomModules();

}
