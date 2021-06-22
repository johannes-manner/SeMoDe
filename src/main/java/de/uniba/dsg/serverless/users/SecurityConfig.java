package de.uniba.dsg.serverless.users;

import de.uniba.dsg.serverless.pipeline.rest.security.CustomAuthenticationProvider;
import de.uniba.dsg.serverless.pipeline.rest.security.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomAuthenticationProvider provider;

    @Bean
    public PasswordEncoder createEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        auth.authenticationProvider(provider);
        auth.userDetailsService(this.userDetailsService).passwordEncoder(this.createEncoder());

    }

    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilterBean() {
        return new JwtAuthenticationTokenFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/users").hasAuthority(Role.ADMIN.getRole())
                .antMatchers("/setups/**").hasAnyAuthority(Role.ADMIN.getRole(), Role.USER.getRole())
                .antMatchers("/api/**").authenticated()
                .antMatchers("/", "/auth/**", "/**").permitAll()
                .and().addFilterBefore(jwtAuthenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class)
                .formLogin().loginPage("/login")
                .and()
                .logout().logoutSuccessUrl("/")
                .and().csrf().ignoringAntMatchers("/auth/**") // needed to access the h2-console after introducing security module;
                .and().csrf().ignoringAntMatchers("/api/**")
                .and().headers().frameOptions().sameOrigin() // needed to access the h2-console after introducing security module
                .and().logout().invalidateHttpSession(true).deleteCookies("JSESSIONID").logoutSuccessUrl("/login");
    }

    /**
     * Only for really simple cases.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login");
    }
}

