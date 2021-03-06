package com.tritoncubed.utilitiesbot.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Subcommand {
    String group() default "";
    String name();
    String description();
}
