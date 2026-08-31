package org.see.skf.core;

import java.util.Set;

public interface AttributeOwnershipListener {

    void releaseRequested(Set<String> candidateAttributeNames);

    void secured(boolean outcome, Set<String> securedAttributeNames);

}
