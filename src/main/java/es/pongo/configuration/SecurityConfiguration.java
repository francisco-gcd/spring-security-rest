package es.pongo.configuration;

import static es.pongo.configuration.util.JsonWebTokenUtil.AUTH_HEADER;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.pongo.configuration.filter.JsonWebTokenAuthenticationFilter;
import es.pongo.configuration.util.JsonWebTokenUtil;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
    private final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);
    
    @Value("${security.jwt.salt}")
	private String jwtSalt;

    @Value("${security.password.salt}")
	private String passwordSalt;

    @Value("${security.password.strength}")
    private int strength;

    @Value("${security.token.live}")
    private long live;

    @Autowired
	UserDetailsService userService;
	
	@Bean
	public PasswordEncoder encryption(){
		return new BCryptPasswordEncoder(strength, new SecureRandom(passwordSalt.getBytes()));
	}
	
    @Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userService);
		provider.setPasswordEncoder(encryption());
		
		auth.authenticationProvider(provider);
		auth.userDetailsService(userService);
    }

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterBefore(new JsonWebTokenAuthenticationFilter(jwtSalt), UsernamePasswordAuthenticationFilter.class)
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		http.csrf().disable();
	       	
		configureMatchers(http);
		configureHandler(http);
	}

	private void sendError(HttpServletResponse response, Exception exception, String message, int status) throws IOException, ServletException{
		if(log.isDebugEnabled()){
			log.debug(message, exception);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", status);
		map.put("message", message);
		map.put("exception", exception.getMessage());
		
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
		
		ObjectMapper mapper = new ObjectMapper();
		response.setStatus(status);
		response.getWriter().write(mapper.writeValueAsString(map));
		response.getWriter().flush();
		response.getWriter().close();		
	}
	
	private void configureHandler(HttpSecurity http) throws Exception {
	       http.logout()
		     .deleteCookies("JSESSIONID")
		     
	       .and().formLogin()
	        .successHandler(new AuthenticationSuccessHandler() {
	    		@Override
	    		public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
	    			
	    			Date now = new Date();
	    			UserDetails user = (UserDetails) authentication.getPrincipal();
	    			StringBuilder scopes = new StringBuilder();
	    			for(GrantedAuthority role : user.getAuthorities()){
	    				scopes.append(role.getAuthority()).append(",");
	    			}
	    			
	    			Map<String, String> values = new HashMap<String, String>();
	    			values.put(JsonWebTokenUtil.JWT_FIELD_SUB, user.getUsername());
	    			values.put(JsonWebTokenUtil.JWT_FIELD_EXP, String.valueOf(now.getTime() + live));
	    			values.put(JsonWebTokenUtil.JWT_FIELD_SCOPES, scopes.substring(0,scopes.length() - 1));
	    			
	    			// Se debe enviar el JWT en la cabecera de la respuesta 
	    			response.setStatus(HttpServletResponse.SC_OK);
	    			response.setHeader(AUTH_HEADER, JsonWebTokenUtil.createToken(values, jwtSalt));
	    			response.getWriter().flush();
	    			response.getWriter().close();		
	    		}
			})
	       
	        .failureHandler(new AuthenticationFailureHandler() {
				@Override
				public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
			    	sendError(response, exception, "Authentication error", HttpServletResponse.SC_UNAUTHORIZED);
				}
			})
	       
	       .and().exceptionHandling()
	       	.authenticationEntryPoint(new AuthenticationEntryPoint() {
			    @Override
				public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
			    	sendError(response, exception, "Not user logged", HttpServletResponse.SC_UNAUTHORIZED);
				}
	       	})
	        .accessDeniedHandler(new AccessDeniedHandler() {
				@Override
				public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
			    	sendError(response, exception, "Not authorized resources", HttpServletResponse.SC_FORBIDDEN);
				}
			});
	}

	private void configureMatchers(HttpSecurity http) throws Exception {
		http.authorizeRequests()
	       .antMatchers(HttpMethod.POST, "/user").permitAll()
	       .antMatchers(HttpMethod.PUT, "/user").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
	       .antMatchers(HttpMethod.DELETE, "/user").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
	       .antMatchers(HttpMethod.GET, "/user/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
	       .antMatchers(HttpMethod.GET, "/user").hasAuthority("ROLE_ADMIN");
	}
}
