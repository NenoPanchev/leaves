package com.example.leaves.config;

import com.example.leaves.service.impl.AppUserDetailService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//    private final AppUserDetailService appUserDetailService;
//    private final PasswordEncoder passwordEncoder;
//
//    public SecurityConfig(AppUserDetailService appUserDetailService, PasswordEncoder passwordEncoder) {
//        this.appUserDetailService = appUserDetailService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        http
//                .csrf().disable();
//        http
//                .httpBasic()
//                .and()
//                .authorizeRequests()
//                .antMatchers("/rest/**").permitAll()
//                .and()
//                .authorizeRequests()
//                .antMatchers("/secure/**").hasAnyRole("ADMIN")
//                .anyRequest().authenticated()
//                .and()
//                .formLogin()
//                .permitAll();
//    }
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .userDetailsService(appUserDetailService)
//                .passwordEncoder(passwordEncoder);
//    }
//}
