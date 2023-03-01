package com.example.leaves.model.entity.config;

import com.example.leaves.service.impl.AppUserDetailService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
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
//                .authorizeRequests()
//                // allow access to static resources to anyone
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
//                // allow access to index, user login and reg to anyone
//                .antMatchers("/", "/users/login", "/users/register").permitAll()
//                .antMatchers("/admin", "/admin/**").hasRole("ADMIN")
//                // protect all other pages
//                .antMatchers("/**").authenticated()
////                .anyRequest().authenticated()
//                .and()
//                // configure login with HTML form
//                .formLogin()
//                // our login page will be served by the controller with mapping /users/login
//                .loginPage("/users/login")
//                // the name of the username input field in OUR login form is username (optional)
//                .usernameParameter("email") // "username"
//                // the name of the password input field in OUR login form is password (optional)
//                .passwordParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY)
//                // on login success redirect here
//                .defaultSuccessUrl("/")
//                // on login failure redirect here
//                .failureForwardUrl("/users/login-error")
//                .and()
//                .rememberMe()
//                .rememberMeParameter("remember")
//                .key("remember Me Encryption Key")
//                .rememberMeCookieName("rememberMeCookieName")
//                .tokenValiditySeconds(10000)
//                .and()
//                // which endpoint performs logout, (should be POST request!!!)
//                .logout()
//                .logoutUrl("/logout")
//                // where to land after logout
//                .logoutSuccessUrl("/")
//                // remove session from the server
//                .invalidateHttpSession(true)
//                // delete the session cookie
//                .deleteCookies("JSESSIONID");
//    }
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .userDetailsService(appUserDetailService)
//                .passwordEncoder(passwordEncoder);
//    }
}
