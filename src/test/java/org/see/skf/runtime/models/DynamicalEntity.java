package org.see.skf.runtime.models;

import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.HLAfloat64LECoder;

@ObjectClass(name = "HLAobjectRoot.PhysicalEntity.DynamicalEntity")
public class DynamicalEntity extends PhysicalEntity {

    @Attribute(name = "force", coder = Vector3Coder.class)
    private Vector3 force;

    @Attribute(name = "mass", coder = HLAfloat64LECoder.class)
    private Double mass;

    @Attribute(name = "mass_rate", coder = HLAfloat64LECoder.class)
    private Double massRate;

    public DynamicalEntity() {
        this.force = new Vector3();
        this.mass = 0.0;
        this.massRate = 0.0;
    }

    public Vector3 getForce() {
        return force;
    }

    public void setForce(Vector3 force) {
        this.force = force;
    }

    public Double getMass() {
        return mass;
    }

    public void setMass(Double mass) {
        this.mass = mass;
    }

    public Double getMassRate() {
        return massRate;
    }

    public void setMassRate(Double massRate) {
        this.massRate = massRate;
    }
}
