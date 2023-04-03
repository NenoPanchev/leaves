package com.example.leaves.config;

import com.example.leaves.config.jwt.AuthEntryPointJwt;
import com.example.leaves.config.jwt.JwtRequestFilter;
import com.example.leaves.config.services.AppUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.example.leaves.constants.GlobalConstants.SWAGGER_WHITELIST;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
//         securedEnabled = true,
//         jsr250Enabled = true,
        prePostEnabled = true)

public class WebSecurityConfig { // extends WebSecurityConfigurer Adapter {   // deprecated from Spring 2.7.0
    private final AppUserDetailService userDetailService;
    private final JwtRequestFilter jwtRequestFilter;
    private final AuthEntryPointJwt unauthorizedHandler;

    public WebSecurityConfig(AppUserDetailService userDetailService, JwtRequestFilter jwtRequestFilter, AuthEntryPointJwt unauthorizedHandler) {
        this.userDetailService = userDetailService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//                .authorizeRequests().antMatchers("/**", "/users/**", "/roles/**", "/departments/**").permitAll().and()
                .authorizeRequests().antMatchers(SWAGGER_WHITELIST).permitAll().and()
                .authorizeRequests().antMatchers("/authenticate").permitAll()
                .anyRequest().authenticated();

//        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
