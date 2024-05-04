package us.lagc.LAGCMemberManagement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import us.lagc.service.UserDetailsServiceImpl;

@SpringBootApplication
@EnableWebSecurity
@ServletComponentScan				// for SessionManagement.java
@ComponentScan(basePackages = {"us.lagc.LAGCMemberManagement", "us.lagc.controller", "us.lagc.service", "us.lagc.data"})			// so that it can find controllers, services and components
@EntityScan("us.lagc.model")
@EnableJpaRepositories(basePackages = {"us.lagc.data"})
public class LagcMemberManagementApplication extends WebMvcConfigurationSupport {

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/static/", "classpath:/resources/"};
//            "classpath:/META-INF/resources/", "classpath:/resources/",
	
	@Autowired
	ProductInterceptor productInterceptor;
	
	private UserDetailsService userDetailsService;
	
	@Autowired
	public void setUserDetailsService(UserDetailsServiceImpl userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public static void main(String[] args) {
		SpringApplication.run(LagcMemberManagementApplication.class, args);
	}
	
	// comes from WebMvcConfigurationSupport
	@Override
	protected void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(productInterceptor);
	}
	
	// comes from WebMvcConfigurationSupport
	// otherwise static resources will not be added automatically
	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
	            .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
	}
	
	/*
	Uncomment to use BCryptPasswordEncoder
	Comment to use Plain Text Password
	*/
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
	    return new MvcRequestMatcher.Builder(introspector);
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
//		 if session expired
		http.sessionManagement(session -> session
				.invalidSessionUrl("/sessionExpired")
				);
		http.sessionManagement(session -> session
				.sessionAuthenticationErrorUrl("/sessionExpired")									// error 402
				);
		
		http
		.csrf(Customizer.withDefaults())															// to avoid 403 Forbidden error for POST requests

		.authorizeHttpRequests( authorize -> authorize
//				.requestMatchers(
//						mvc.pattern("/admin/properties")
//					).hasRole("ADMIN")		// only allow users with authentication and role to specific url pattern, order matters
				.requestMatchers(
						mvc.pattern("/error"),
						mvc.pattern("/sessionExpired"),
						mvc.pattern("/img/**"),
						mvc.pattern("/css/**")
//						mvc.pattern("/**")		// // to permit all urls without authentication
					).permitAll()				// URLS that can pass without authentication, order matters
				
				.anyRequest().authenticated()										// otherwise allow all request with any role but should be authenticated
				)

		.httpBasic(Customizer.withDefaults())														// tells Spring to use HTTP Basic Authentication
		
		.formLogin( form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/member", true)
				.permitAll()
				)
		.logout( logout -> logout
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.invalidateHttpSession(false)													// so that it will not overwrite next url to invalidSessionUrl provided above
				.logoutSuccessUrl("/login")
				.permitAll());
		
		return http.build();
    }
	
	@Bean
	public AuthenticationManager authenticationManager() throws Exception {						//	https://stackoverflow.com/a/75168585
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(this.userDetailsService);
        /*
    	Uncomment to use BCryptPasswordEncoder
    	Comment to use Plain Text Password
        */
        authProvider.setPasswordEncoder(passwordEncoder());
        List<AuthenticationProvider> providers =  List.of(authProvider);
        return new ProviderManager(providers);
	}

}

//https://stackoverflow.com/a/65272395 - HOW TO INSERT PLAIN PASSWORD FOR BASIC AUTH - if your plaintext password is abc123ABC you will have to store it in database as {noop}abc123ABC
//https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
//https://howtodoinjava.com/spring-boot2/security-rest-basic-auth-example/ - .httpBasic() IMP

//	@Bean
//    public InMemoryUserDetailsManager userDetailsService() {
//		UserDetails[] users = new UserDetails[Constants.fromEmailIds.length];
//		for (int i = 0; i < users.length; i++) {
//			users[i] = User.withUsername(Constants.fromEmailIds[i])
//					.password(passwordEncoder().encode(Constants.fromEmailIdAuthKey[i]))
//					.roles("USER")
//					.build();
//		}
//
//		return new InMemoryUserDetailsManager(users);
//    }
	