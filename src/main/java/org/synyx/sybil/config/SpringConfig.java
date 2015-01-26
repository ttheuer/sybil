package org.synyx.sybil.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Spring Configuration.
 *
 * @author  Tobias Theuer - theuer@synyx.de
 */

@Configuration
@ComponentScan(basePackages = "org.synyx.sybil") // scan for annotated classes, like @Service, @Configuration, etc.
public class SpringConfig {
}