package com.example.oauth2.web.client.config;

import com.example.oauth2.web.client.custom.CustomDefaultWebResponseExceptionTranslator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Created by mike on 2019/7/9
 */
@Configuration
public class ClientSecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails userDetails = User.withUsername("user")
                .password(passwordEncoder.encode("000"))
                .roles("USER", "ADMIN", "CLIENT")
                .build();
        return new InMemoryUserDetailsManager(userDetails);
    }

    @Configuration
    @EnableAuthorizationServer
    static class CustomAuthorizationServer extends AuthorizationServerConfigurerAdapter {

        private final PasswordEncoder encoder;

        private final AuthenticationManager authenticationManager;

        private final UserDetailsService userDetailsService;

        public CustomAuthorizationServer(PasswordEncoder encoder,
                                         AuthenticationManager authenticationManager,
                                         UserDetailsService userDetailsService) {
            this.encoder = encoder;
            this.authenticationManager = authenticationManager;
            this.userDetailsService = userDetailsService;
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
            oauthServer.tokenKeyAccess("permitAll()")
                    .checkTokenAccess("isAuthenticated()")
                    .allowFormAuthenticationForClients();
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
            endpoints.authenticationManager(authenticationManager)
                    // password模式支持
                    .userDetailsService(userDetailsService)
                    .exceptionTranslator(new CustomDefaultWebResponseExceptionTranslator());
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
                    .withClient("client_1")
                    .secret(encoder.encode("$2a$10$JknlOkbQANofGnc9BRkLv.Kuixt/pZleX2VC54udsy5Gqry7iSFzK"))
                    .scopes("userinfo", "resource")
                    .resourceIds("oauth2-resource")
                    .autoApprove(true)
                    .accessTokenValiditySeconds(1200)
                    .refreshTokenValiditySeconds(50000)
                    .authorizedGrantTypes("authorization_code", "password", "implicit", "refresh_token", "client_credentials")
                    .redirectUris("http://www.server.com:8000/login/oauth2/code/client",
                            "http://www.server.com:8000/login/oauth2/code/github",
                            "http://www.server.com:9020/login/oauth2/code/client",
                            "http://www.server.com:9020/login/oauth2/code/github",
                            "https://www.taobao.com",
                            "https://www.jd.com");
        }
    }

    @Configuration
    @EnableResourceServer
    static class CustomResourceServer extends ResourceServerConfigurerAdapter {

        private final AccessDeniedHandler accessDeniedHandler;

        private final AuthenticationEntryPoint authenticationEntryPoint;

        public CustomResourceServer(AccessDeniedHandler accessDeniedHandler,
                                    AuthenticationEntryPoint authenticationEntryPoint) {
            this.accessDeniedHandler = accessDeniedHandler;
            this.authenticationEntryPoint = authenticationEntryPoint;
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId("oauth2-resource")
                    //自定义Token异常信息,用于token校验失败返回信息
                    .authenticationEntryPoint(authenticationEntryPoint)
                    //授权异常处理
                    .accessDeniedHandler(accessDeniedHandler);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
                    // 跨域访问
                    .cors().and()
                    .authorizeRequests()
                    .antMatchers("/admin").hasRole("ADMIN")
                    // 客户端授权资源认证
                    .antMatchers("/userinfo").access("#oauth2.hasScope('userinfo')")
                    .antMatchers("/resource").access("#oauth2.hasScope('resource')")
                    .anyRequest().fullyAuthenticated().and()
                    .formLogin().and().httpBasic();
        }
    }

    @EnableWebSecurity
    static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        private final AccessDeniedHandler accessDeniedHandler;

        private final AuthenticationEntryPoint authenticationEntryPoint;

        public WebSecurityConfig(AccessDeniedHandler accessDeniedHandler,
                                 AuthenticationEntryPoint authenticationEntryPoint) {
            this.accessDeniedHandler = accessDeniedHandler;
            this.authenticationEntryPoint = authenticationEntryPoint;
        }

        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http
                    .exceptionHandling()
                    .accessDeniedHandler(accessDeniedHandler)
                    .authenticationEntryPoint(authenticationEntryPoint);
        }

    }
}
