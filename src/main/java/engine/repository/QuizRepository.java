package engine.repository;

import engine.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.awt.print.Pageable;
import java.util.List;

//public interface QuizRepository extends PagingAndSortingRepository<Quiz, Integer> {
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
   // Page<Quiz> findAll(Pageable pageable);
}
