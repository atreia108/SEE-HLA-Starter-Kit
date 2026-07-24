package org.see.skf.runtime;

import hla.rti1516_2025.encoding.EncoderFactory;
import org.see.skf.core.SKUtilityFactory;
import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.Coder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class SKAnnotatedTypeParser {

    // All coders used by the federate are cached here, so that eventually we won't have to instantiate any new coders (which can be a computationally expensive process, since it uses the reflection API).
    private final Map<Class<? extends Coder<?>>, Coder<?>> coderInstancePool;
    private final Map<Class<? extends Coder<?>>, Method[]> coderClassToMethods;

    public SKAnnotatedTypeParser() {
        this.coderInstancePool = new HashMap<>();
        this.coderClassToMethods = new HashMap<>();
    }

    /*
    ParseResult parse(Object parseableObject) throws AnnotationParseException {
        return new ParseResult(parseableObject);
    }
     */

    public HLAObjectInstance parseObject(Object parseableObject) throws AnnotationParseException {
        return null;
    }

    public HLAInteractionData parseInteraction(Object parseableObject) throws AnnotationParseException {
        return null;
    }

    final class ParseResult {
        private final Object target;

        private ParseResult(Object target) {
            this.target = target;
        }
    }
    /*

    final class ParseResult {

        private final Object targetObject;

        // Name in the HLA FOM such as "HLAobjectRoot.PhysicalEntity" or "HLAinteractionRoot.ModeTransitionRequest".
        private final String fomClassName;

        // As per the enforced JavaBeans standard requirement for all simulation class models, there has to be a getter and setter for every field
        // that holds data for its corresponding attribute/parameter in the FOM.
        // Method[0] = getter and Method[1] = setter
        private final Map<String, Method[]> attributeNameToAccessors;

        private final Map<String, Coder<?>> attributeToCoders;

        private final Map<Class<?>, Method[]> coderToMethods;

        private ParseResult(Object object) {
            this.targetObject = object;

            ObjectClass objectClass = object.getClass().getAnnotation(ObjectClass.class);
            InteractionClass interactionClass = object.getClass().getAnnotation(InteractionClass.class);
            checkAnnotationSuitability(objectClass, interactionClass);

            if (objectClass != null) {
                this.fomClassName = objectClass.name();
            } else {
                this.fomClassName = interactionClass.name();
            }

            this.attributeNameToAccessors = new HashMap<>();
            this.attributeToCoders = new HashMap<>();
            this.coderToMethods = new HashMap<>();

            build();
        }

        private void checkAnnotationSuitability(ObjectClass objectClass, InteractionClass interactionClass) {
            if (objectClass != null && interactionClass != null) {
                throw new AnnotationParseException("<" + targetObject + "> has both an @ObjectClass or @InteractionClass annotation attached.");
            } else if (objectClass == null && interactionClass == null) {
                throw new AnnotationParseException("<" + targetObject + "> because has neither @ObjectClass nor @InteractionClass annotation attached.");
            }
        }

        private void build() {
            Class<?> targetClass = targetObject.getClass();

            while (targetClass != Object.class && targetClass.getAnnotation(ObjectClass.class) != null) {
                processClass(targetClass);

                // Continuously move up the object hierarchy.
                targetClass = targetClass.getSuperclass();
            }
        }

        private void processClass(Class<?> clazz) {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Attribute.class)) {
                    Attribute objectClassAttribute = field.getAnnotation(Attribute.class);
                    String attributeName = objectClassAttribute.name();

                    String getterName = "get" + capitalize(attributeName);
                    String setterName = "set" + capitalize(attributeName);
                    Method[] accessorMethods = new Method[]{
                            retrieveMethod(clazz, getterName),
                            retrieveMethod(clazz, setterName)
                    };

                    Coder<?> attributeCoder = getAndCreateCoderIfAbsent(objectClassAttribute.coder());
                    Class<?> coderClass = attributeCoder.getClass();

                    Method[] coderMethods = new Method[]{
                            retrieveMethod(coderClass, "encode"),
                            retrieveMethod(coderClass, "decode")
                    };

                    this.attributeNameToAccessors.put(attributeName, accessorMethods);
                    this.attributeToCoders.put(attributeName, attributeCoder);
                    this.coderToMethods.put(coderClass, coderMethods);
                }
            }
        }

        private Coder<?> getAndCreateCoderIfAbsent(Class<? extends Coder<?>> clazz) {
            Coder<?> coderInstance;

            if (coderPool.containsKey(clazz)) {
                coderInstance = coderPool.get(clazz);
            } else {
                try {
                    EncoderFactory encoderFactory = SKUtilityFactory.INSTANCE.getEncoderFactory();
                    coderInstance = clazz.getDeclaredConstructor(EncoderFactory.class).newInstance(encoderFactory);

                    coderPool.put(clazz, coderInstance);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new AnnotationParseException("Problem instantiating the coder <" + clazz + ">. Ensure that a public constructor that accepts EncoderFactory as parameter is present.", e);
                } catch (InvocationTargetException e) {
                    throw new AnnotationParseException("Problem instantiating the coder <" + clazz + ">. An unexpected exception was thrown by its constructor.", e);
                }
            }

            return coderInstance;
        }

        private Method[] getCoderMethodsIfAbsent(C)

        private Method retrieveMethod(Class<?> clazz, String methodName, Class<?>... parameterType) {
            try {
                return (parameterType.length > 0) ? clazz.getDeclaredMethod(methodName, parameterType) : clazz.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                throw new AnnotationParseException("Required method " + methodName + "() is not defined for the class <" + clazz.getName() + ">.");
            }
        }

        private String capitalize(String word) {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }

        String getFomClassName() {
            return this.fomClassName;
        }

        Set<String> getAttributeNames() {
            return this.attributeNameToAccessors.keySet();
        }

        Method[] getAttributeAccessors(String attributeName) {
            return this.attributeNameToAccessors.get(attributeName);
        }

        Map<String, Method[]> getAttributeAccessors() {
            return this.attributeNameToAccessors;
        }

        Map<String, Coder<?>> getAttributeCoders() {
            return this.attributeToCoders;
        }

        Coder<?> getAttributeCoder(String attributeName) {
            return this.attributeToCoders.get(attributeName);
        }

        Map<Coder<?>, Method[]> getCoderMethods() {
            return this.coderToMethods;
        }

        Method[] getAttributeCoderMethods(Coder<?> coder) {
            return this.coderToMethods.get(coder);
        }

        Object getTargetObject() {
            return this.targetObject;
        }
    }
     */
}
