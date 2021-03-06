package com.chen.f.admin.configuration.security;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启chen-f spring security配置
 *
 * @author chen
 * @since 2019/1/15 0:13.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        SpringSecurityConfiguration.class,
        WebSpringSecurityConfiguration.class,
        ReactiveWebSpringSecurityConfiguration.class,
})
public @interface EnableChenFSpringSecurity {

}
