package es.pongo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.pongo.domain.Role;
import es.pongo.domain.User;
import es.pongo.exception.ServiceException;
import es.pongo.repository.UserRepository;

@Service
public class UserService implements UserDetailsService{

	@Autowired
	private PasswordEncoder encryption;

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username);
	}
	
	public User register(User user) throws ServiceException{
		if(user.getAuthorities().contains(new Role("ROLE_ADMIN")) && userRepository.findByAuthoritiesAuthority("ROLE_ADMIN") != null){
			throw new ServiceException(HttpStatus.BAD_REQUEST, "ROLE_ADMIN role already exists");
		}
		
		if (userRepository.findByUsername(user.getUsername()) != null) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "A user exist with the same username");
		}

		user.setPassword(encryption.encode(user.getPassword()));
		user.setEnabled(Boolean.TRUE);

		return userRepository.save(user);
	}
	
	public User update(User user){
		user.setPassword(encryption.encode(user.getPassword()));
		return userRepository.save(user);
	}
	
	public User find(String id){
		return userRepository.findOne(id);
	}

	public Iterable<User> findAll(Pageable pageable){
		return userRepository.findAll(pageable);
	}
	
	public void remove(String id){
		userRepository.delete(id);
	}
}
