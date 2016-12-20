package es.pongo.configuration.util;

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
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonWebTokenUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonWebTokenUtil.class);

    private static final String HMAC_ALGO = "HmacSHA256";
	private static final String SEPARATOR = ".";
	private static final String SEPARATOR_SPLITTER = "\\.";

	private static final String JWT_FIELD_ALG = "alg";
	private static final String JWT_FIELD_TYP = "typ";
	
	public static final String JWT_FIELD_SUB = "sub";
	public static final String JWT_FIELD_EXP = "exp";
	public static final String JWT_FIELD_SCOPES = "scopes";
	
	public static final String AUTH_HEADER = "X-Auth-Token";

	public static boolean isValidtoken(String token, String secret){
    	
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
    
    public static Map<String, String> decode(String token){
    	Map<String, String> values = new HashMap<String, String>();
    	
    	String[] aux = token.split(SEPARATOR_SPLITTER);
    	if(aux != null && aux.length == 3){
    		try {
        		ObjectMapper mapper = new ObjectMapper();
        		String json = new String(Base64.getDecoder().decode(aux[1]), StandardCharsets.UTF_8);
        		values = mapper.readValue(json, Map.class);        		
			} catch (IOException oops) {
				log.error("JWTService.decodeUser : Error al recuperar el usuario del token", oops);
			}
    	}
    	
    	return values;
    }

    public static String createToken(Map<String, String> values, String secret){
		StringBuilder token = new StringBuilder();
		
		try {
			String header = makeHeader();
			String payload = makePayload(values);
			String signature = makeSignature(header, payload, secret);
			
			token = token.append(header).append(SEPARATOR).append(payload).append(SEPARATOR).append(signature);
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | JsonProcessingException oops) {
			log.error("JWTService.createToken : Error al generar el token", oops);
		}
		
		return token.toString();
	}
	
	private static String makeHeader() throws JsonProcessingException{
		Map<String, String> values = new HashMap<String, String>();
		values.put(JWT_FIELD_ALG, "HS256");
		values.put(JWT_FIELD_TYP, "JWT");
		
		return Base64.getEncoder().encodeToString(new ObjectMapper().writeValueAsString(values).getBytes(StandardCharsets.UTF_8));
	}
	
	private static String makePayload(Map<String, String> values) throws JsonProcessingException{	
		return Base64.getEncoder().encodeToString(new ObjectMapper().writeValueAsString(values).getBytes(StandardCharsets.UTF_8));
	}
	
	private static String makeSignature(String header, String payload, String secret) throws NoSuchAlgorithmException, InvalidKeyException{
		Mac hmac = Mac.getInstance(HMAC_ALGO);
		hmac.init(new SecretKeySpec(secret.getBytes(), HMAC_ALGO));
		
		String toEncode = header + SEPARATOR + payload;
		
		return Base64.getEncoder().encodeToString(hmac.doFinal(toEncode.getBytes(StandardCharsets.UTF_8)));
	}
}
