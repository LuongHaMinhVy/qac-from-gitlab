package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.BadWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadWordRepository extends JpaRepository<BadWord, Long> {

    List<BadWord> findAllByIsActiveTrue();

    boolean existsByWordIgnoreCase(String word);
}
