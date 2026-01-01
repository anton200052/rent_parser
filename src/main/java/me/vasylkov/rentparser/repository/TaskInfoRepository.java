package me.vasylkov.rentparser.repository;

import me.vasylkov.rentparser.entity.TaskInfo;
import me.vasylkov.rentparser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskInfoRepository extends JpaRepository<TaskInfo, Long> {

    @Modifying
    @Query("UPDATE TaskInfo t SET t.iterations = t.iterations + 1 WHERE t.id = :id AND t.user = :user")
    void incrementIterations(@Param("id") Long id, @Param("user") User user);

    @Modifying
    @Query("UPDATE TaskInfo t SET t.active = CASE t.active WHEN true THEN false ELSE true END WHERE t.id = :id AND t.user = :user")
    void toggleActive(@Param("id") Long id, @Param("user") User user);

    Optional<TaskInfo> findByIdAndUser(Long id, User user);

    void deleteByIdAndUser(Long id, User user);

    List<TaskInfo> findByUser(User user);

    List<TaskInfo> findAllByActiveTrue();
}
