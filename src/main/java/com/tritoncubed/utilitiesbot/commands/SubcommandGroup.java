package com.tritoncubed.utilitiesbot.commands;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface SubcommandGroups {
    SubcommandGroup[] value();
}

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SubcommandGroups.class)
public @interface SubcommandGroup {
    String name();
    String description();
}
