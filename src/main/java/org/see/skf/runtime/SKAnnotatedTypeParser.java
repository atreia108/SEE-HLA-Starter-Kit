package org.see.skf.runtime;

import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.Coder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SKAnnotatedTypeParser {

    private final HLAClassManager classManager;
    private final Map<Class<Coder<?>>, Object> coderPool;

    public SKAnnotatedTypeParser(HLAClassManager classManager) {
        this.classManager = classManager;
        this.coderPool = new ConcurrentHashMap<>();
    }

    ParseResult parseObjectInstanceData(Object parseableObject) throws AnnotationParseException {
        return new ParseResult(parseableObject);
    }

    ParseResult parseInteractionData(Object parseableObject) throws AnnotationParseException {
        return new ParseResult(parseableObject);
    }

    static final class ParseResult {

        private final Object targetObject;

        // Name in the HLA FOM such as "HLAobjectRoot.PhysicalEntity" or "HLAinteractionRoot.ModeTransitionRequest".
        private final String fomClassName;

        // Each attribute/parameter corresponding to the Java field of the class.
        // For instance, we may have a class such as PhysicalEntity with the fields parentReferenceFrame and bodyWrtStructural.
        // In the FOM, these are designated as "parent_reference_frame" and "body_wrt_structural". So this mapping reconciles that difference.
        private final Map<String, Field> fomElementToField;

        // As per the enforced JavaBeans standard requirement for all simulation class models, there has to be a getter/setter for every field
        // that holds data for its corresponding attribute/parameter in the FOM.
        private final Map<Field, Method[]> fieldToGetterSetter;

        private final Map<Field, Coder<?>> fieldToCoder;

        ParseResult(Object object) {
            this.targetObject = object;

            ObjectClass objectClass = object.getClass().getAnnotation(ObjectClass.class);
            InteractionClass interactionClass = object.getClass().getAnnotation(InteractionClass.class);
            checkAnnotationSuitability(objectClass, interactionClass);

            if (objectClass != null) {
                this.fomClassName = objectClass.name();
            } else {
                this.fomClassName = interactionClass.name();
            }

            this.fomElementToField = new HashMap<>();
            this.fieldToGetterSetter = new HashMap<>();
            this.fieldToCoder = new HashMap<>();

            build();
        }

        private void checkAnnotationSuitability(ObjectClass objectClass, InteractionClass interactionClass) {
            if (objectClass != null && interactionClass != null) {
                throw new AnnotationParseException("Cannot build internalized representation for <" + targetObject + "> because it has both an @ObjectClass or @InteractionClass annotation attached.");
            } else if (objectClass == null && interactionClass == null) {
                throw new AnnotationParseException("Cannot build internalized representation for <" + targetObject + "> because it does not have an @ObjectClass or @InteractionClass annotation attached.");
            }
        }

        private void build() {
            Field[] fields = targetObject.getClass().getDeclaredFields();
            // TODO - Treatment for fields is simple: Get all ancestral fields including methods and make them accessible in addition to the ones for this class.
        }

        String getFomClassName() {
            return this.fomClassName;
        }

        Map<String, Field> getFomElementFields() {
            return this.fomElementToField;
        }

        Map<Field, Method[]> getFieldMethods() {
            return this.fieldToGetterSetter;
        }

        Map<Field, Coder<?>> getFieldCoders() {
            return this.fieldToCoder;
        }
    }
}
