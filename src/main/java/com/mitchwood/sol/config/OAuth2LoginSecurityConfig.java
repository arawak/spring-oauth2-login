package com.mitchwood.sol.config;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import com.mitchwood.sol.user.User;
import com.mitchwood.sol.user.UserService;

@Configuration
@EnableWebSecurity
public class OAuth2LoginSecurityConfig {

	@Autowired
	UserService userService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize -> {
			    authorize.requestMatchers("/favicon.ico").permitAll();
                authorize.anyRequest().authenticated();
			})
			.oauth2Login(oauth2 -> oauth2
			        .userInfoEndpoint(userInfo ->
			            userInfo.oidcUserService(oidcUserService())
			            )
			        )
			.formLogin(Customizer.withDefaults())
			;
		return http.build();
	}

	OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();
 
        return (userRequest) -> {
            // Delegate to the default implementation for loading a user
            OidcUser oidcUser = delegate.loadUser(userRequest);

			// we want to know which IdP we are using, and what their user id is
			String registrationId = userRequest.getClientRegistration().getRegistrationId(); 
    		String subClaim = userRequest.getIdToken().getSubject();

			User user = userService.getUserByRegistrationAndSubject(registrationId, subClaim);

			OidcUserInfo userInfo = oidcUser.getUserInfo();

			Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
			if (user != null) {
				Set<String> roles = userService.getRolesForUser(user);
				for (String role : roles) {
					mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_"+ role));
				}

				userInfo = new OidcUserInfo(Map.of("username", user.getUsername()));
			}

			// now we'll create a new OidcUser based on the old one, but we'll replace
			// the mapped authorities and we'll use the username as the Principal
			// name
			oidcUser = new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), 
				userInfo, "username");

            return oidcUser;
        };
    }
}