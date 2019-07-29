package org.kingeasy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kingeasy.base.Statistics;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Statistic {
	String key() default "total";
	Statistics statistic() default Statistics.COUNT;
	boolean handleNull() default false;
}
