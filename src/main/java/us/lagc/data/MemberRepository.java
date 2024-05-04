package us.lagc.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import us.lagc.model.Member;

@Repository
public interface MemberRepository extends CrudRepository<Member, Integer> {
	
	List<Member> findAll();
	Optional<List<Member>> findByPhone(Long phone);
	Optional<List<Member>> findByFirstNameContainingAndLastNameContaining(String firstName, String lastName);
	Optional<List<Member>> findByFirstNameContaining(String firstName);
	Optional<List<Member>> findByLastNameContaining(String lastName);

}
