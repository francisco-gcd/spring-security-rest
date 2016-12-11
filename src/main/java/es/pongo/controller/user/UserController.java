package es.pongo.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.pongo.controller.GenericController;
import es.pongo.domain.User;
import es.pongo.exception.ServiceException;
import es.pongo.service.UserService;

@RestController("authenticated.usercontroller")
@RequestMapping(value = "user")
public class UserController extends GenericController{

	@Autowired
	private UserService userService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new UserValidator());
	}

	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<User> find(@PathVariable String id, Authentication authentication){
    	User user = userService.find(id);
	    return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Iterable<User>> findAll(){
    	Iterable<User> users = userService.findAll();
	    return new ResponseEntity<Iterable<User>>(users, HttpStatus.OK);
	}

	@PreAuthorize("permitAll()")
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<User> create(@Validated @RequestBody User user) throws ServiceException {
		user = userService.register(user);
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<User> update(@Validated @RequestBody User user, BindingResult bindingResult, Authentication authentication) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException, SecurityException {

		if(StringUtils.isEmpty(user.getId())){
			bindingResult.rejectValue("id", "id.empty");
		}
		
		if(bindingResult.hasErrors()){
			throw new MethodArgumentNotValidException(new MethodParameter(User.class.getConstructor(), 0), bindingResult);
		}
		
		user = userService.update(user);
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	public void remove(@PathVariable String id, Authentication authentication) throws ServiceException {
		userService.remove(id);
	}
}