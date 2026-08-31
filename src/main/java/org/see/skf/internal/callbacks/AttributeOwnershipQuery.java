package org.see.skf.internal.callbacks;

import hla.rti1516_2025.AttributeHandle;
import hla.rti1516_2025.AttributeHandleSet;
import hla.rti1516_2025.ObjectInstanceHandle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class AttributeOwnershipQuery {

    private final ObjectInstanceHandle instanceHandle;

    private final Map<AttributeHandle, Boolean> handleToAcquisitionStatus;

    private final Map<AttributeHandle, String> handleToFederateName;

    AttributeOwnershipQuery(ObjectInstanceHandle instanceHandle, AttributeHandleSet set) {
        this.instanceHandle = instanceHandle;

        this.handleToAcquisitionStatus = new ConcurrentHashMap<>();
        set.forEach(handle -> this.handleToAcquisitionStatus.put(handle, false));

        this.handleToFederateName = new ConcurrentHashMap<>();
    }

    boolean isCompleted() {
        for (boolean status : this.handleToAcquisitionStatus.values()) {
            if (!status) {
                return false;
            }
        }

        return true;
    }

    void inform(AttributeHandleSet set, String ownerName) {
        set.forEach(handle -> {
            if (this.handleToAcquisitionStatus.containsKey(handle)) {
                this.handleToFederateName.put(handle, ownerName);
                this.handleToAcquisitionStatus.replace(handle, true);
            }
        });
    }

    ObjectInstanceHandle getInstanceHandle() {
        return this.instanceHandle;
    }

    Map<AttributeHandle, String> getResult() {
        return this.handleToFederateName;
    }
}
