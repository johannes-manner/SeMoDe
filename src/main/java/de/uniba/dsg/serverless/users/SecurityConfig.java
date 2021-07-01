package de.uniba.dsg.serverless.users;

import de.uniba.dsg.serverless.pipeline.rest.security.CustomAuthenticationProvider;
import de.uniba.dsg.serverless.pipeline.rest.security.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder createEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Order(1)
    @Configuration
    public class ApiSecurityAdapter extends WebSecurityConfigurerAdapter {

        private UserDetailsService userDetailsService;
        private CustomAuthenticationProvider provider;
        private PasswordEncoder encoder;

        @Autowired
        public ApiSecurityAdapter(UserDetailsService userDetailsService, CustomAuthenticationProvider provider, PasswordEncoder encoder) {
            this.userDetailsService = userDetailsService;
            this.provider = provider;
            this.encoder = encoder;
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            super.configure(auth);
            auth.authenticationProvider(provider);
            auth.userDetailsService(userDetailsService).passwordEncoder(encoder);

        }

        @Bean
        public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilterBean() {
            return new JwtAuthenticationTokenFilter();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .authorizeRequests()
                    .antMatchers("/api/login").permitAll()
                    .antMatchers("/api/**").authenticated()
                    .and()
                    .addFilterBefore(jwtAuthenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class)
                    .csrf().ignoringAntMatchers("/api/**")
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

    }

    @Order(2)
    @Configuration
    public class WebSecurityAdapter extends WebSecurityConfigurerAdapter {

        private UserDetailsService userDetailsService;
        private PasswordEncoder encoder;

        @Autowired
        public WebSecurityAdapter(UserDetailsService userDetailsService, PasswordEncoder encoder) {
            this.userDetailsService = userDetailsService;
            this.encoder = encoder;
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            super.configure(auth);
            auth.userDetailsService(userDetailsService).passwordEncoder(encoder);

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/users").hasAuthority(Role.ADMIN.getRole())
                    .antMatchers("/setups/**", "/semode/**").hasAnyAuthority(Role.ADMIN.getRole(), Role.USER.getRole())
                    .antMatchers("/", "/index").permitAll()
                    .and()
                    .csrf().ignoringAntMatchers("/semode/**")
                    .and()
                    .formLogin().loginPage("/login")
                    .and()
                    .logout().logoutSuccessUrl("/")
                    .and().logout().invalidateHttpSession(true).deleteCookies("JSESSIONID").logoutSuccessUrl("/login");
        }
    }
}

