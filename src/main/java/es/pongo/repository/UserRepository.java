package es.pongo.repository;

import org.springframework.data.repository.CrudRepository;

import es.pongo.domain.User;

public interface UserRepository extends CrudRepository<User, String>{

	public User findByUsername(String username);
}
