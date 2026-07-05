package org.springframework.samples.petclinic.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static jakarta.servlet.DispatcherType.ERROR;
import static jakarta.servlet.DispatcherType.FORWARD;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${petclinic.admin.username:admin}")
	private String adminUsername;

	@Value("${petclinic.admin.password}")
	private String adminPassword;

	@Value("${petclinic.user.username:user}")
	private String userUsername;

	@Value("${petclinic.user.password}")
	private String userPassword;

	@Bean
	SecurityFilterChain web(HttpSecurity http) throws Exception {

		http.httpBasic(withDefaults()).formLogin(withDefaults())
			.csrf(c -> c.csrfTokenRepository(new CookieCsrfTokenRepository()));

		http.authorizeHttpRequests(authorize -> authorize.dispatcherTypeMatchers(FORWARD, ERROR)
			.permitAll()
			.requestMatchers("/diagnostics/**")
			.hasRole("ADMIN")
			.requestMatchers("/customers/**")
			.hasRole("USER")
			.requestMatchers("/owners/**")
			.hasRole("ADMIN")
			.anyRequest()
			.authenticated());

		http.headers(headers -> headers
			.contentSecurityPolicy(csp -> csp
				.policyDirectives("default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'"))
			.contentTypeOptions(withDefaults())
			.xssProtection(withDefaults()));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		if (userPassword == null || adminPassword == null) {
			throw new IllegalStateException(
				"PetClinic passwords must be set via PETCLINIC_ADMIN_PASSWORD and PETCLINIC_USER_PASSWORD environment variables");
		}

		UserDetails userDetails = User.builder()
			.username(userUsername)
			.password(passwordEncoder.encode(userPassword))
			.roles("USER")
			.build();

		UserDetails adminDetails = User.builder()
			.username(adminUsername)
			.password(passwordEncoder.encode(adminPassword))
			.roles("ADMIN", "USER")
			.build();

		return new InMemoryUserDetailsManager(userDetails, adminDetails);
	}

}
