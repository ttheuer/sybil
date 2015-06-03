package org.synyx.sybil.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Spring Configuration.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */

@Profile("default")
@Configuration
@PropertySource(value = { "classpath:config.properties" })
@EnableScheduling
@ComponentScan(
    basePackages = { "org.synyx.sybil" }, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebConfig.class)
    }
) // scan for annotated classes, like @Service, @Configuration, etc. - while ignoring the WebConfig, which is loaded by the ApiWebAppInitializer
public class SpringConfig {

    @Bean
    ObjectMapper mapper() {

        return new ObjectMapper();
    }
}
