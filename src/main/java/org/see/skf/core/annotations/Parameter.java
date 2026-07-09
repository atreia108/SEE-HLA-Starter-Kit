package org.see.skf.core.annotations;

import org.see.skf.encoding.Coder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TBA
 *
 * @see InteractionClass
 * @since 1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {
    String name();

    Class<? extends Coder<?>> coder();
}
