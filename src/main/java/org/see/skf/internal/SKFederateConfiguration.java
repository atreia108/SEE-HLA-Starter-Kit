package org.see.skf.internal;

import org.see.skf.core.SKFederate;

public interface SKFederateConfiguration {

    String rtiAddress();

    String federationName();

    String federateName();

    String federateType();

    long lookahead();

    int maxThreads();

    String[] additionalFomModules();

    SKFederate.Role federateRole();

}
