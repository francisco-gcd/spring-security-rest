package es.pongo.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import es.pongo.domain.User;

public interface UserRepository extends PagingAndSortingRepository<User, String>{

	public User findByUsername(String username);
	public User findByAuthoritiesAuthority(String authority);
}
