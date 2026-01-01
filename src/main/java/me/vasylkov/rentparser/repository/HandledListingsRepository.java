package me.vasylkov.rentparser.repository;

import me.vasylkov.rentparser.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HandledListingsRepository extends JpaRepository<Listing, String> {

    @Query("SELECT l.id FROM Listing l WHERE l.id IN :ids")
    List<String> findExistingIds(@Param("ids") List<String> ids);
}
