package es.pongo.controller.user;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import es.pongo.domain.User;

public class UserValidator implements Validator{

	private static final int MAX_LENGTH = 16;
	private static final int MIN_LENGTH = 8;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "username.empty");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.empty");
		
		User user = (User) target;
		if(user.getUsername() != null && user.getUsername().length() > MAX_LENGTH){
			errors.rejectValue("username", "username.max");
		}

		if(user.getPassword() != null && user.getPassword().length() > MAX_LENGTH){
			errors.rejectValue("password", "password.max");
		}

		if(user.getPassword() != null && user.getPassword().length() < MIN_LENGTH){
			errors.rejectValue("password", "password.min");
		}
		
		if(user.getAuthorities() == null){
			errors.rejectValue("authorities", "authorities.null");
		}
	}
}
