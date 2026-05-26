package org.see.skf.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aa.
 *
 * @see ObjectClass
 * @since 1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attribute {
    /**
     *
     * @return
     */
    String name();

    /**
     *
     * @return
     */
    Class<? extends Coder<?>> coder();
}
