package es.pongo.configuration.filter;

import static es.pongo.configuration.util.JsonWebTokenUtil.AUTH_HEADER;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.pongo.configuration.util.JsonWebTokenUtil;
import es.pongo.domain.Role;

public class JsonWebTokenAuthenticationFilter extends GenericFilterBean  {

	private String secret;
	
	public JsonWebTokenAuthenticationFilter(String secret) {
		this.secret = secret;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String token = ((HttpServletRequest) request).getHeader(AUTH_HEADER);
		if(!StringUtils.isEmpty(token)){
			if(JsonWebTokenUtil.isValidtoken(token, secret)){
				Map<String, String> values = JsonWebTokenUtil.decode(token);
				
				Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
				for(String authority : Arrays.asList(values.get(JsonWebTokenUtil.JWT_FIELD_SCOPES).split(","))){
					authorities.add(new Role(authority));
				}

				if(System.currentTimeMillis() < Long.valueOf(values.get(JsonWebTokenUtil.JWT_FIELD_EXP))){
					sendError((HttpServletResponse) response);
					return;
				}
				
				Authentication authentication = new UsernamePasswordAuthenticationToken(values.get(JsonWebTokenUtil.JWT_FIELD_SUB), "", authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		
		chain.doFilter(request, response);
	}
	
	private void sendError(HttpServletResponse response) throws JsonProcessingException, IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", HttpServletResponse.SC_UNAUTHORIZED);
		map.put("message", "not user logged");
		map.put("exception", "token expired");
		
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
		
		ObjectMapper mapper = new ObjectMapper();
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write(mapper.writeValueAsString(map));
		response.getWriter().flush();
		response.getWriter().close();		
	}
}
