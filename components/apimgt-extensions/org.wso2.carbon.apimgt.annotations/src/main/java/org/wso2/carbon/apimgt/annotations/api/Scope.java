package org.wso2.carbon.apimgt.annotations.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class is the representation of custom developed Scopes annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

    String name();

    String description();

    String key();

    String[] permissions();

}
