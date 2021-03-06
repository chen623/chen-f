package com.chen.f.common.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * 默认开启redis http session
 *
 * @author chen
 * @since 2017/12/20 8:41.
 */
@Configuration
@ConditionalOnClass(RedisOperations.class)
@AutoConfigureBefore({SessionAutoConfiguration.class})
@EnableRedisHttpSession
public class EnableChenFCommonSpringSessionConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpSessionIdResolver httpSessionIdResolver() {
        //return HeaderHttpSessionIdResolver.xAuthToken();
        return new CookieHttpSessionIdResolver();
    }

}
