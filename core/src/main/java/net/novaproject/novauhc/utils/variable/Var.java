package net.novaproject.novauhc.utils.variable;

import net.novaproject.novauhc.lang.Lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Var {

    String name() default "";

    String desc() default "";

    Class<? extends Lang> lang() default Lang.class;

    String nameKey() default "";

    String descKey() default "";

    VariableType type() default VariableType.INFER;

    double min() default Double.NaN;

    double max() default Double.NaN;

    boolean common() default false;

    String category() default "";
}
