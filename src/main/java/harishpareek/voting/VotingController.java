package harishpareek.voting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

@Controller
public class VotingController {
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping(value = "/vote", produces = {"application/json"})
    public ResponseEntity vote(@RequestParam(value = "id", required = false) Long candidateId
            , @RequestParam(value = "fname", required = false) String firstName
            , @RequestParam(value = "lname", required = false) String lastName
            , @RequestParam(value = "op", required = false, defaultValue = "1") Integer opinion) {
        if (opinion == null || opinion < -1 || opinion == 0 || opinion > 1) {
            return new ResponseEntity(Collections.singletonMap("error", "Invalid opinion; must be either -1 or 1"), HttpStatus.BAD_REQUEST);
        }
        Optional<Candidate> candidate = null;
        if (candidateId != null) {
            // lookup candidate by id
            candidate = candidateRepository.findById(candidateId);
        } else {
            if (StringUtils.isEmpty(firstName) || StringUtils.isEmpty(lastName)) {
                return new ResponseEntity(Collections.singletonMap("error", "Invalid candidate name"), HttpStatus.BAD_REQUEST);
            }
            candidate = candidateRepository.findByFirstNameAndLastName(firstName, lastName);
        }
        if (!candidate.isPresent()) {
            return new ResponseEntity(Collections.singletonMap("error", "Candidate not found"), HttpStatus.BAD_REQUEST);
        }
        Vote vote = new Vote();
        vote.setCandidate(candidate.get());
        vote.setOpinion(opinion);
        voteRepository.save(vote);
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(vote.getId()).toUri());
        return new ResponseEntity(vote, resHeaders, HttpStatus.CREATED);
    }

    @GetMapping(value = "/total", produces = {"application/json"})
    public ResponseEntity total() {
        List<Result> results = new ArrayList<>();
        jdbcTemplate.query("SELECT c.first_name || ' ' || c.last_name candidate,COALESCE(SUM(v.opinion),0) votes FROM candidate c LEFT OUTER JOiN vote v ON c.id = v.candidate_id  GROUP BY c.first_name, c.last_name"
                , (resultSet, i) -> new Result(resultSet.getString("candidate"), resultSet.getInt("votes")))
                .forEach(result -> results.add(result));
        return new ResponseEntity(results, HttpStatus.OK);
    }
    @GetMapping(value = "/total/{id}", produces = {"application/json"})
    public ResponseEntity total(@PathVariable(required=true) Long id) {
        Optional<Candidate> c = candidateRepository.findById(id);
        if(!c.isPresent()) {
            return new ResponseEntity(Collections.singletonMap("error", "Candidate with id " + id + " not found"), HttpStatus.BAD_REQUEST);
        }
        List<Result> results = new ArrayList<>();
        jdbcTemplate.query("SELECT c.first_name || ' ' || c.last_name candidate,COALESCE(SUM(v.opinion),0) votes FROM candidate c LEFT OUTER JOIN vote v ON c.id = v.candidate_id  WHERE c.id = ?"
                , new Object[] {id}
                , (resultSet, i) -> new Result(resultSet.getString("candidate"), resultSet.getInt("votes")))
                .forEach(result -> results.add(result));
        return new ResponseEntity(results.get(0), HttpStatus.OK);
    }

}
