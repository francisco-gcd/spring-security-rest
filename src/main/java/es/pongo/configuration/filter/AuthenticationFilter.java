package es.pongo.configuration.filter;

import static es.pongo.service.JsonWebTokenService.AUTH_HEADER;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import es.pongo.service.JsonWebTokenService;

public class AuthenticationFilter extends GenericFilterBean {

	private String secret;
	
	private JsonWebTokenService jsonWebTokenService;
	
	public AuthenticationFilter(JsonWebTokenService jsonWebTokenService, String secret) {
		this.jsonWebTokenService = jsonWebTokenService;
		this.secret = secret;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		Authentication authentication = null;
		String token = ((HttpServletRequest) request).getHeader(AUTH_HEADER);
		if(!StringUtils.isEmpty(token)){
			if(jsonWebTokenService.isValidtoken(token, secret)){
				UserDetails user = jsonWebTokenService.decodeUser(token);
				if(user != null){
					authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
				}
			}
		}
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}
}
