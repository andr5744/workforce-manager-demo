package com.example.workforce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration using the WebSecurityConfigurerAdapter pattern.
 *
 * NOTE FOR MIGRATION: WebSecurityConfigurerAdapter is deprecated in Spring Security 5.7
 * and REMOVED in Spring Security 6.x (Spring Boot 3.x).
 * Migration requires rewriting this class using the SecurityFilterChain bean approach
 * and updating antMatchers() -> requestMatchers().
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
            .and()
            .withUser("manager")
                .password(passwordEncoder().encode("manager123"))
                .roles("MANAGER")
            .and()
            .withUser("employee")
                .password(passwordEncoder().encode("employee123"))
                .roles("EMPLOYEE");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/api/departments/**").hasAnyRole("ADMIN", "MANAGER")
                .antMatchers(org.springframework.http.HttpMethod.GET, "/api/employees/**").authenticated()
                .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "MANAGER")
                .antMatchers("/api/projects/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                .antMatchers("/api/time-entries/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                .antMatchers("/api/reports/**").hasAnyRole("ADMIN", "MANAGER")
                .antMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .httpBasic()
            .and()
            .headers().frameOptions().sameOrigin(); // required for H2 console iframe
    }
}
