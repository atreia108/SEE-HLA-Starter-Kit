package org.see.skf.runtime;

import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.Coder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SKAnnotatedTypeParser {

    private final HLAClassManager classManager;
    private final Map<Class<Coder<?>>, Object> coderPool;

    public SKAnnotatedTypeParser(HLAClassManager classManager) {
        this.classManager = classManager;
        this.coderPool = new ConcurrentHashMap<>();
    }

    public HLAObjectInstance createObjectInstanceRepresentation(Object objectInstance) throws AnnotationParseException {
        ObjectClass annotation = getObjectClassAnnotation(objectInstance);
        String className = annotation.name();
        HLAObjectClass objectClass = classManager.getObjectClass(className);

        if (objectClass != null) {
            return null;
        } else {
            // N.B. How do we know that this exception was thrown because the user has not published the object/interaction class?
            // The federate never receives any update that would warrant the creation of an object instance representation until it has been subscribed to.
            // In which case, the framework will have handled everything; this also means that the object class will show up in the HLAClassManager because
            // it was already created for subscription purposes.
            throw new AnnotationParseException("Cannot create object instance for the HLA object class <" + className + "> because it has not been published yet.");
        }
    }

    private ObjectClass getObjectClassAnnotation(Object objectInstance) throws AnnotationParseException {
        try {
            return objectInstance.getClass().getAnnotation(ObjectClass.class);
        } catch (NullPointerException e) {
            throw new AnnotationParseException("Cannot register object instance for an object that is NULL.");
        }
    }

    public HLAInteractionData createInteractionRepresentation(Object interaction) {
        InteractionClass annotation = getInteractionClassAnnotation(interaction);

        return null;
    }

    private InteractionClass getInteractionClassAnnotation(Object interaction) throws AnnotationParseException {
        try {
            return interaction.getClass().getAnnotation(InteractionClass.class);
        } catch (NullPointerException e) {
            throw new AnnotationParseException("Cannot create an interaction for an object that is NULL.");
        }
    }
}
