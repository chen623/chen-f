package com.chen.f.spring.boot.configuration;

import com.chen.f.spring.boot.configuration.errorhandle.EnableChenFErrorHandle;
import com.chen.f.spring.boot.configuration.i18n.EnableChenFI18n;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启chen-f核心配置
 *
 * @author chen
 * @since 2019/1/15 0:13.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        EnableChenFCommonConfiguration.class,
        EnableChenFCommonConfigurationImportSelector.class,
})
@EnableChenFI18n
@EnableChenFErrorHandle
@EnableCaching
public @interface EnableChenFCommon {
}
