package org.see.skf.internal.runtime;

import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.core.annotations.Parameter;
import org.see.skf.encoding.Coder;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public final class SKAnnotatedTypeParser2 {

    private final CoderManager coderManager;

    public SKAnnotatedTypeParser2(CoderManager coderManager) {
        this.coderManager = coderManager;
    }

    public Metadata parseInteractionProxy(Class<?> proxyClass) {
        isNull(proxyClass);
        return buildInteractionStructure(proxyClass);
    }

    private void isNull(Object proxy) {
        if (proxy == null) {
            throw new NullPointerException("NULL reference provided for parsing.");
        }
    }

    private void illegalMultiAnnotationCheck(Class<?> clazz) {
        ObjectClass a1 = clazz.getAnnotation(ObjectClass.class);
        ObjectClass a2 = clazz.getAnnotation(ObjectClass.class);

        if (a1 != null && a2 != null) {
           throw new AnnotationParseException(clazz + " uses @ObjectClass and @InteractionClass annotation which is disallowed.");
        }
    }

    private Metadata buildInteractionStructure(Class<?> clazz) {
        InteractionClass fomNameAnnotation = clazz.getAnnotation(InteractionClass.class);
        if (fomNameAnnotation == null) {
            throw new AnnotationParseException("No @InteractionClass annotation attached for <" + clazz.getName() + ">.");
        }

        Metadata metadata = new Metadata(clazz, fomNameAnnotation.name());
        while (clazz != Object.class && clazz.isAnnotationPresent(InteractionClass.class)) {
            illegalMultiAnnotationCheck(clazz);

            for (Field field : clazz.getDeclaredFields()) {
                Parameter parameter = field.getAnnotation(Parameter.class);

                if (parameter != null) {
                    String parameterName = parameter.name();
                    Class<? extends Coder<?>> coderClass = parameter.coder();
                    CoderManager.CoderReflectionData coderData = this.coderManager.get(coderClass);

                    Trait t = new Trait.Builder()
                            .sourceClass(clazz)
                            .field(field)
                            .annotatedName(parameterName)
                            .coderData(coderData)
                            .build();

                    metadata.add(t, clazz);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return metadata;
    }

    public static final class Metadata {

        private final Class<?> proxyClass;

        private final String fomClassName;

        private final Set<Trait> traits;

        private Metadata(Class<?> proxyClass, String fomClassName) {
            this.proxyClass = proxyClass;
            this.fomClassName = fomClassName;
            this.traits = new HashSet<>();
        }

        private void add(Trait trait, Class<?> forClass) {
            String name = trait.getAssignedFieldName();

            if (traitExists(name)) {
                throw new AnnotationParseException("The member <" + name + "> is duplicated in the hierarchy of the class <" + forClass.getName() + ">.");
            }

            this.traits.add(trait);
        }

        private boolean traitExists(String name) {
            return this.traits.stream().anyMatch(trait -> trait.getAssignedFieldName().equals(name));
        }

        public Class<?> getProxyClass(){
            return this.proxyClass;
        }

        public String getFomClassName() {
            return this.fomClassName;
        }

        public Set<Trait> getTraits() {
            return this.traits;
        }
    }
}
