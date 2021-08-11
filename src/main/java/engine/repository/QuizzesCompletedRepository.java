package engine.repository;

import engine.entity.QuizzesCompleted;
import engine.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

public interface QuizzesCompletedRepository extends JpaRepository<QuizzesCompleted, Integer> {
   Page<QuizzesCompleted> findByName(String name, Pageable pageable);
}
