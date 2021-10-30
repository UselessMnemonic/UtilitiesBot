package com.tritoncubed.utilitiesbot.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandOption {
    String name();
    String description() default "";
    boolean required() default true;
}
