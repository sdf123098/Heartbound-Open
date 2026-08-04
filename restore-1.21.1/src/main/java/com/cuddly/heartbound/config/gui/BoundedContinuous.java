package com.cuddly.heartbound.config.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface BoundedContinuous {
   int precision() default 2;

   double min() default 0.0;

   double max();
}
