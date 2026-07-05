package org.springframework.samples.emailservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${emailservice.password}")
	private String emailServicePassword;

	@Bean
	SecurityFilterChain web(HttpSecurity http) throws Exception {
		http.httpBasic(withDefaults()).formLogin(withDefaults())
			.csrf(c -> c.ignoringAntMatchers("/registerEmail"));

		http.authorizeHttpRequests(authorize -> authorize
			.antMatchers("/actuator/health", "/actuator/info")
			.permitAll()
			.anyRequest()
			.authenticated());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		UserDetails serviceUser = User.builder()
			.username("emailservice")
			.password(passwordEncoder.encode(emailServicePassword))
			.roles("EMAIL_SERVICE")
			.build();
		return new InMemoryUserDetailsManager(serviceUser);
	}

}