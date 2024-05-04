package us.lagc.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import us.lagc.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
	User findByUsername(String username);
	User findByEmail(String adminemail);
}
