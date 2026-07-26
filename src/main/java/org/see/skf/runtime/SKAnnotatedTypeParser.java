package org.see.skf.runtime;

import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.core.annotations.Parameter;
import org.see.skf.encoding.Coder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public final class SKAnnotatedTypeParser {

    private final CoderManager coderManager;

    public SKAnnotatedTypeParser(CoderManager coderManager) {
        this.coderManager = coderManager;
    }

    public ParsedStructure parseObjectInstance(Object parseableObject) {
        isNull(parseableObject);
        return buildObjectInstanceStructure(parseableObject);
    }

    public ParsedStructure parseInteraction(Object parseableObject) {
        isNull(parseableObject);
        return buildInteractionStructure(parseableObject);
    }

    private void isNull(Object targetObject){
        if (targetObject == null) {
            throw new AnnotationParseException("Cannot parse the structure of an object that is NULL.");
        }
    }

    private ParsedStructure buildObjectInstanceStructure(Object parseableObject){
        Class<?> clazz = parseableObject.getClass();
        ObjectClass annotation = isObjectClass(clazz);
        String objectClassName = annotation.name();
        ParsedStructure objectClassStructure = new ParsedStructure(parseableObject, objectClassName);

        while (clazz != Object.class && clazz.isAnnotationPresent(ObjectClass.class)) {
            evalMultiAnnotation(clazz);

            for (Field field : clazz.getDeclaredFields()) {
                Attribute attribute = field.getAnnotation(Attribute.class);
                if (attribute != null) {
                    Trait t = new Trait(field)
                            .withName(attribute.name())
                            .ofClass(clazz)
                            .withCoder(attribute.coder());

                    objectClassStructure.add(t);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return objectClassStructure;
    }

    private ObjectClass isObjectClass(Class<?> clazz){
        ObjectClass annotation = clazz.getAnnotation(ObjectClass.class);

        if (annotation == null) {
            throw new AnnotationParseException(clazz + " does not have an @ObjectClass annotation attached.");
        }

        return annotation;
    }

    private void evalMultiAnnotation(Class<?> clazz) {
        ObjectClass a1 = clazz.getAnnotation(ObjectClass.class);
        InteractionClass a2 = clazz.getAnnotation(InteractionClass.class);

        if (a1 != null && a2 != null) {
            throw new AnnotationParseException("Confusing attachment of both @ObjectClass and @InteractionClass annotations on <." + clazz + ">.");
        }
    }

    private ParsedStructure buildInteractionStructure(Object parseableObject) {
        Class<?> clazz = parseableObject.getClass();
        InteractionClass annotation = isInteractionClass(parseableObject.getClass());
        String interactionClassName = annotation.name();
        ParsedStructure interactionClassStructure = new ParsedStructure(parseableObject, interactionClassName);

        while (clazz != Object.class && clazz.isAnnotationPresent(InteractionClass.class)) {
            evalMultiAnnotation(clazz);

            for (Field field : clazz.getDeclaredFields()) {
                Parameter parameter = field.getAnnotation(Parameter.class);
                if (parameter != null) {
                    Trait t = new Trait(field)
                            .withName(parameter.name())
                            .ofClass(clazz)
                            .withCoder(parameter.coder());

                    interactionClassStructure.add(t);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return interactionClassStructure;
    }

    private InteractionClass isInteractionClass(Class<?> clazz){
        InteractionClass annotation = clazz.getAnnotation(InteractionClass.class);

        if (annotation == null) {
            throw new AnnotationParseException("The class " + clazz + " does not have an @InteractionClass annotation attached.");
        }

        return annotation;
    }

    public final class ParsedStructure {

        private final Object targetObject;

        // Name in the HLA FOM such as "HLAobjectRoot.PhysicalEntity" or "HLAinteractionRoot.ModeTransitionRequest".
        private final String classNameInFom;

        // A trait refers to an attribute if this class is an HLA object class, or a parameter if this is an HLA interaction class.
        private final Set<Trait> traits;

        private ParsedStructure(Object targetObject, String classNameInFom) {
            this.targetObject = targetObject;
            this.classNameInFom = classNameInFom;

            this.traits = new HashSet<>();
        }

        private void add(Trait trait) {
            this.traits.add(trait);
        }

        public Object getTargetObject() {
            return this.targetObject;
        }

        public String getClassNameInFom() {
            return this.classNameInFom;
        }

        public Set<Trait> getTraits() {
            return this.traits;
        }
    }

    public final class Trait {

        private final Field field;

        private String name;

        private Method[] accessors;

        private CoderManager.CoderReflectionData coderReflectionData;

        Trait(Field field) {
            this.field = field;
        }

        private Method[] retrieveAccessors(Class<?> clazz) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            String capitalizedFieldName = capitalize(fieldName);

            String getterName = "get" + capitalizedFieldName;
            String setterName = "set" + capitalizedFieldName;

            try {
                Method getterMethod = clazz.getDeclaredMethod(getterName);
                Method setterMethod = clazz.getDeclaredMethod(setterName, fieldType);

                return new Method[] { getterMethod, setterMethod };
            } catch (NoSuchMethodException e) {
                throw new AnnotationParseException("The field <" + fieldName + "> either lacks one or more accessor (getter/setter) methods or these methods do not operate with the field's type <" + fieldType + ">.", e);
            }
        }

        private String capitalize(String word) {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }

        private Trait withName(String name) {
            this.name = name;
            return this;
        }

        private Trait ofClass(Class<?> clazz) {
            this.accessors = retrieveAccessors(clazz);
            return this;
        }

        private Trait withCoder(Class<? extends Coder<?>> clazz) {
            this.coderReflectionData = coderManager.get(clazz);

            Class<?> fieldType = field.getType();
            Class<?> genericType = this.coderReflectionData.genericType();

            if (!fieldType.equals(genericType)) {
                String fieldName = field.getName();
                throw new AnnotationParseException("Field <" + fieldName + "> expected a coder for type <" + fieldType + "> but got <" + genericType + "> instead.");
            }

            return this;
        }

        public String name() {
            return this.name;
        }

        public Method getter() {
            return this.accessors[0];
        }

        public Method setter() {
            return this.accessors[1];
        }

        public Coder<?> coder() {
            return this.coderReflectionData.coder();
        }
    }
}
