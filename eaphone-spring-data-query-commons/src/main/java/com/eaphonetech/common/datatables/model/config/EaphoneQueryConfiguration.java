package com.eaphonetech.common.datatables.model.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.eaphonetech.common.datatables.model.mapping.QueryInput;

/**
 * Registers a {@link HandlerMethodArgumentResolver} that builds up {@link QueryInput} from web requests.
 * 
 * @author Xiaoyu Guo
 */
@Configuration
public class EaphoneQueryConfiguration implements WebMvcConfigurer {

    @Lazy
    @Bean
    public DataTablesArgumentResolver dataTablesArgumentResolver() {
        return new DataTablesArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(0, dataTablesArgumentResolver());
    }
}
