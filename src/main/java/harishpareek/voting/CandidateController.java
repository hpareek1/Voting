package harishpareek.voting;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class CandidateController {
    private final static Logger logger = Logger.getLogger(CandidateController.class);
    @Autowired
    private CandidateRepository repository;

    @PostMapping(value = "/candidate", produces = {"application/json"})
    public ResponseEntity create(@RequestParam(name = "fname", required = true) String firstName
            , @RequestParam(name = "lname", required = true) String lastName) {
        Optional<Candidate> candidate = repository.findByFirstNameAndLastName(firstName, lastName);
        if(candidate.isPresent()) {
            return new ResponseEntity(Collections.singletonMap("error", "Candidate "+ firstName + " " + lastName + " already exists."), HttpStatus.BAD_REQUEST);
        }
        Candidate c = new Candidate();
        c.setFirstName(firstName);
        c.setLastName(lastName);
        c.setCreation(new Date());
        repository.save(c);
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(c.getId()).toUri());
        return new ResponseEntity(c, resHeaders, HttpStatus.CREATED);

    }
    @GetMapping(value="/candidate/{id}", produces = {"application/json"})
    public ResponseEntity retrieve(@PathVariable(required = true) Long id) {
        Optional<Candidate> c = repository.findById(id);
        if(!c.isPresent()) {
            return new ResponseEntity(Collections.singletonMap("message", "Candidate with id " + id + " does not exist"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(c.get(), HttpStatus.OK);
    }

    @GetMapping(value = "/candidate", produces = {"application/json"})
    public ResponseEntity list() {
        return new ResponseEntity(repository.findAll(), HttpStatus.OK);
    }
}
