package harishpareek.voting;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends CrudRepository<Candidate, Long>{
    @Query("SELECT c FROM Candidate AS c ORDER BY c.creation")
    List<Candidate> findAll();
    @Query("SELECT c FROM Candidate AS c WHERE c.firstName = :firstName AND c.lastName = :lastName")
    Optional<Candidate> findByFirstNameAndLastName(@Param("firstName")String firstName, @Param("lastName") String lastName);
}
