package org.kingeasy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kingeasy.base.TableRelation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Mapping {
	TableRelation tableRelation() default TableRelation.ONE_TO_ONE;
	String[] value() default {};
}
