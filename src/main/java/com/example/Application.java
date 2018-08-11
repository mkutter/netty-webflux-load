package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

@SpringBootApplication
@EnableWebFlux
public class Application {
	public static void main(String... args) {
		SpringApplication application = new SpringApplication();
		application.setWebApplicationType(WebApplicationType.REACTIVE);
		SpringApplication.run(Application.class, args);
	}

	@EnableWebFluxSecurity
	public static class SecurityConfiguration {
		@Bean
		public SecurityWebFilterChain securityWebFilterChain(
				ServerHttpSecurity http) {
			return http.requestCache().disable()
					.csrf().disable()
					.formLogin().disable()
					.logout().disable()
					.headers().disable()
					.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
					.authorizeExchange()
					.anyExchange()
					.authenticated().and()
					.httpBasic().and()
					.build();
		}

		@Bean
		public ReactiveUserDetailsService userDetailsService() {
			return s -> Mono.just(new UserDetails() {
				public Collection<? extends GrantedAuthority> getAuthorities() {
					return Collections.EMPTY_LIST;
				}

				public String getPassword() {
					return "{noop}PASSWORD";
				}

				public String getUsername() {
					return "USERNAME";
				}

				public boolean isAccountNonExpired() {
					return false;
				}

				public boolean isAccountNonLocked() {
					return false;
				}

				public boolean isCredentialsNonExpired() {
					return false;
				}

				public boolean isEnabled() {
					return true;
				}
			});
		}
	}
}
