package harishpareek.voting;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;

import java.awt.*;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VotingApplicationTests {
    private final static Logger logger = LoggerFactory.getLogger(VotingApplicationTests.class);
    @Autowired
    private TestRestTemplate restTemplate;
    private Random random = new Random();

    @Test
    public void canAddCandidate() {
        final String firstName = "firstName-" + random.nextLong();
        final String lastName = "lastName-" + random.nextLong();
        ResponseEntity<Map> response = restTemplate.exchange("/candidate?fname={firstName}&lname={lastName}", HttpMethod.POST, null, Map.class, firstName, lastName);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        Map candidate = response.getBody();
        assertThat(response.getHeaders().getLocation()).isNotNull().toString().endsWith(candidate.get("id").toString());

        response = restTemplate.getForEntity("/candidate/{id}", Map.class, candidate.get("id"));
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map lookedUpCandidate = response.getBody();
        assertThat(lookedUpCandidate.get("id")).isEqualTo(candidate.get("id"));
        assertThat(lookedUpCandidate.get("firstName")).isEqualTo(candidate.get("firstName"));
        assertThat(lookedUpCandidate.get("lastName")).isEqualTo(candidate.get("lastName"));
    }

    @Test
    public void canVoteForCandidates() {
        final int votesToCast = 3;
        final Candidate candidate1 = new Candidate("Candi-" + random.nextLong(), "One-" + random.nextLong());
        final Candidate candidate2 = new Candidate("Candi-" + random.nextLong(), "Two-" + random.nextLong());
        final Candidate candidate3 = new Candidate("Candi-" + random.nextLong(), "Three-" + random.nextLong());

        final int c1UpVotes = random.nextInt(votesToCast);
        final int c1DownVotes = random.nextInt(votesToCast);

        final int c2UpVotes = random.nextInt(votesToCast);
        final int c2DownVotes = random.nextInt(votesToCast);

        final int c3UpVotes = random.nextInt(votesToCast);
        final int c3DownVotes = random.nextInt(votesToCast);


        ResponseEntity<Map> resp = restTemplate.exchange("/candidate?fname={firstName}&lname={lastName}", HttpMethod.POST, null, Map.class, candidate1.getFirstName(), candidate1.getLastName());
        candidate1.setId(Long.valueOf(resp.getBody().get("id").toString()));

        resp = restTemplate.exchange("/candidate?fname={firstName}&lname={lastName}", HttpMethod.POST, null, Map.class, candidate2.getFirstName(), candidate2.getLastName());
        candidate2.setId(Long.valueOf(resp.getBody().get("id").toString()));

        resp = restTemplate.exchange("/candidate?fname={firstName}&lname={lastName}", HttpMethod.POST, null, Map.class, candidate3.getFirstName(), candidate3.getLastName());
        candidate3.setId(Long.valueOf(resp.getBody().get("id").toString()));

        for (int i = 0; i < c1UpVotes; i++) {
            restTemplate.exchange("/vote?fname={firstName}&lname={lastName}&op=1", HttpMethod.POST, null, Map.class, candidate1.getFirstName(), candidate1.getLastName());

        }
        for (int i = 0; i < c1DownVotes; i++) {
            restTemplate.exchange("/vote?fname={firstName}&lname={lastName}&op=-1", HttpMethod.POST, null, Map.class, candidate1.getFirstName(), candidate1.getLastName());

        }

        for (int i = 0; i < c2UpVotes; i++) {
            restTemplate.exchange("/vote?fname={firstName}&lname={lastName}&op=1", HttpMethod.POST, null, Map.class, candidate2.getFirstName(), candidate2.getLastName());

        }
        for (int i = 0; i < c2DownVotes; i++) {
            restTemplate.exchange("/vote?fname={firstName}&lname={lastName}&op=-1", HttpMethod.POST, null, Map.class, candidate2.getFirstName(), candidate2.getLastName());

        }

        for (int i = 0; i < c3UpVotes; i++) {
            restTemplate.exchange("/vote?fname={firstName}&lname={lastName}&op=1", HttpMethod.POST, null, Map.class, candidate3.getFirstName(), candidate3.getLastName());

        }
        for (int i = 0; i < c3DownVotes; i++) {
            restTemplate.exchange("/vote?fname={firstName}&lname={lastName}&op=-1", HttpMethod.POST, null, Map.class, candidate3.getFirstName(), candidate3.getLastName());

        }

        Map response = restTemplate.getForObject("/total/{id}", Map.class, candidate1.getId());
        assertThat(response.get("votes")).isEqualTo(c1UpVotes-c1DownVotes);

        response = restTemplate.getForObject("/total/{id}", Map.class, candidate2.getId());
        assertThat(response.get("votes")).isEqualTo(c2UpVotes-c2DownVotes);

        response = restTemplate.getForObject("/total/{id}", Map.class, candidate3.getId());
        assertThat(response.get("votes")).isEqualTo(c3UpVotes-c3DownVotes);

    }

}
