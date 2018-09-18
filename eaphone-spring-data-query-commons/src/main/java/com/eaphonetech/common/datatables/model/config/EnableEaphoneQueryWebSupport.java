package com.eaphonetech.common.datatables.model.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Annotation to automatically register the following beans for usage with Spring MVC. Note that using this annotation
 * will require Spring 3.2.
 * @author Xiaoyu Guo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Inherited
@Import(EnableEaphoneQueryWebSupport.Activator.class)
public @interface EnableEaphoneQueryWebSupport {

    static class Activator implements ImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[] { EaphoneQueryConfiguration.class.getName() };
        }

    }
}
