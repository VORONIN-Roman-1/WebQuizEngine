package engine.controller;

import engine.entity.QuizzesCompleted;
import engine.repository.QuizRepository;
import engine.repository.QuizzesCompletedRepository;
import engine.repository.UsersRepository;
import engine.entity.Quiz;
import engine.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
@RestController
public class QuizController {

    AnswerToChecker success = new AnswerToChecker(true, "Congratulations, you are right!");
    AnswerToChecker fail = new AnswerToChecker(false, "Wrong answer! Please, try again.");

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizzesCompletedRepository quizzesCompletedRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping(path = "api/quizzes/{id}")
    public Quiz getQuiz(@PathVariable Integer id) {
        return quizRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "entity not found"));
    }

    @GetMapping(value = "api/quizzes")
    public Page<Quiz> getAllQuizzes( @RequestParam(defaultValue = "0") Integer page,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(defaultValue = "id") String sortBy
          //  @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortBy));
      //  Pageable firstPageWithTwoElements = PageRequest.of(0, 2);
        return quizRepository.findAll(pageable);
    }

    @PostMapping(path = "api/quizzes", consumes = "application/json")
    public Quiz postQuiz(@Valid @RequestBody Quiz quiz) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        System.out.println("User= " + username);
        quiz.setUsername(username);
        quizRepository.save(quiz);
        return quiz;
    }

    @PostMapping(path = "api/quizzes/{id}/solve", consumes = "application/json")
    public AnswerToChecker answerQuiz(@PathVariable Integer id, @RequestBody Checker checker) {
        if (checker.getAnswer().equals(quizRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RRR entity not found")).getAnswer())) {
            String name= SecurityContextHolder.getContext().getAuthentication().getName();
            QuizzesCompleted quizzesCompleted= new QuizzesCompleted(id, name, new Date());
            quizzesCompletedRepository.save(quizzesCompleted);
            return success;
        }
        else{ return fail;}
    }
    @GetMapping(path = "api/quizzes/completed")
    public Page<QuizzesCompleted> getQuizzesCompleted( @PageableDefault(sort = {"completedAt"}, direction = Sort.Direction.DESC) Pageable pageable){
       //return quizzesCompletedRepository.findAll(pageable);
        String name= SecurityContextHolder.getContext().getAuthentication().getName();
       return quizzesCompletedRepository.findByName(name, pageable);

    }

    @PostMapping(path = "api/register", consumes = "application/json")
    public void registerQuiz(@Valid @RequestBody Users users) {
        Users users1 = usersRepository.findByEmail(users.getEmail());
        if (users1 != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        usersRepository.save(users);
    }

    @GetMapping(value = "api/register")
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    @DeleteMapping(path = "/api/quizzes/{id}")
    public ResponseEntity<Integer> deleteQuiz(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (quiz.getUsername().equals(username)) {
            quizRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}

class Checker {
    Set<Integer> answer;

    public Checker() {
    }

    public Set<Integer> getAnswer() {
        return answer;
    }

    public void setAnswer(Set<Integer> answer) {
        this.answer = answer;
    }
}

class AnswerToChecker {
    private Boolean success;
    private String feedback;

    public AnswerToChecker(Boolean success, String feedback) {
        this.success = success;
        this.feedback = feedback;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}

