package es.pongo.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.pongo.domain.User;

@Service
public class JsonWebTokenService {
    private static final Logger log = LoggerFactory.getLogger(JsonWebTokenService.class);

    private static final String HMAC_ALGO = "HmacSHA256";
	private static final String SEPARATOR = ".";
	private static final String SEPARATOR_SPLITTER = "\\.";
	
	private static final String JWT_FIELD_ALG = "alg";
	private static final String JWT_FIELD_TYP = "typ";
	
	private static final String JWT_FIELD_USERNAME = "username";
	private static final String JWT_FIELD_AUTHORITIES = "authorities";
	
	public static final String AUTH_HEADER = "X-Auth-Token";
    
    public boolean isValidtoken(String token, String secret){
    	
    	if(StringUtils.isEmpty(token) || StringUtils.isEmpty(secret)){
    		return Boolean.FALSE;
    	}
    	
    	boolean valid = Boolean.FALSE;
    	
    	String[] values = token.split(SEPARATOR_SPLITTER);
    	if(values != null && values.length == 3){
        	
        	try {
				String signature = makeSignature(values[0], values[1], secret);
				valid = signature.equals(values[2]);
				
			} catch (InvalidKeyException | NoSuchAlgorithmException oops) {
				log.error("JWTService.isValidtoken : Error al validar el token", oops);
			}
    	}
    	
    	return valid;
    }
    
    public UserDetails decodeUser(String token){
    	if(StringUtils.isEmpty(token)){
    		return null;
    	}
    	
    	UserDetails user = null;
    	String[] values = token.split(SEPARATOR_SPLITTER);
    	if(values != null && values.length == 3){
    		try {
        		ObjectMapper mapper = new ObjectMapper();
        		String json = new String(Base64.getDecoder().decode(values[1]), StandardCharsets.UTF_8);
				user = mapper.readValue(json, User.class);
			} catch (IOException oops) {
				log.error("JWTService.decodeUser : Error al recuperar el usuario del token", oops);
			}
    	}
    	
    	return user;
    }
    
    public String createToken(UserDetails user, String secret){
    	if(user == null || StringUtils.isEmpty(secret)){
    		return "";
    	}
		
		StringBuilder token = new StringBuilder();
		
		try {
			String header = makeHeader();
			String payload = makePayload(user);
			String signature = makeSignature(header, payload, secret);
			
			token = token.append(header).append(SEPARATOR).append(payload).append(SEPARATOR).append(signature);
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | JsonProcessingException oops) {
			log.error("JWTService.createToken : Error al generar el token", oops);
		}
		
		return token.toString();
	}
	
	private String makeHeader() throws JsonProcessingException{
		Map<String, String> values = new HashMap<String, String>();
		values.put(JWT_FIELD_ALG, "HS256");
		values.put(JWT_FIELD_TYP, "JWT");
		
		return Base64.getEncoder().encodeToString(new ObjectMapper().writeValueAsString(values).getBytes(StandardCharsets.UTF_8));
	}
	
	private String makePayload(UserDetails user) throws JsonProcessingException{
		Map<String, Object> values = new HashMap<String, Object>();
		values.put(JWT_FIELD_USERNAME, user.getUsername());
		values.put(JWT_FIELD_AUTHORITIES, user.getAuthorities());
		
		return Base64.getEncoder().encodeToString(new ObjectMapper().writeValueAsString(values).getBytes(StandardCharsets.UTF_8));
	}
	
	private String makeSignature(String header, String payload, String secret) throws NoSuchAlgorithmException, InvalidKeyException{
		Mac hmac = Mac.getInstance(HMAC_ALGO);
		hmac.init(new SecretKeySpec(secret.getBytes(), HMAC_ALGO));
		
		String toEncode = header + SEPARATOR + payload;
		
		return Base64.getEncoder().encodeToString(hmac.doFinal(toEncode.getBytes(StandardCharsets.UTF_8)));
	}
}
