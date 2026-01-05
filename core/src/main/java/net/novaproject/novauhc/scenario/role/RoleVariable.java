package net.novaproject.novauhc.scenario.role;


import net.novaproject.novauhc.utils.VariableType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RoleVariable {
    String name();
    String description() default "";
    VariableType type();


}
