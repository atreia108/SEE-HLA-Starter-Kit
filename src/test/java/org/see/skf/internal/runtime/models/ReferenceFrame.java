package org.see.skf.internal.runtime.models;

import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.HLAunicodeStringCoder;

@ObjectClass(name = "HLAobjectRoot.ReferenceFrame")
public final class ReferenceFrame {
    @Attribute(name = "name", coder = HLAunicodeStringCoder.class)
    private String name;

    @Attribute(name = "parent_name", coder = HLAunicodeStringCoder.class)
    private String parentName;

    public ReferenceFrame() {
        this.name = "";
        this.parentName = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}
