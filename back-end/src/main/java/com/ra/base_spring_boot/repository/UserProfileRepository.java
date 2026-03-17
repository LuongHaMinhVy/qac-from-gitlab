package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<User, Integer> {

    Optional<User> findByAccount_AccountId(Integer accountId);

    Optional<User> findByAccount_Username(String username);

    Optional<User> findByAccount_Email(String email);

    boolean existsByAccount_AccountId(Integer accountId);

    @Query("SELECT up FROM User up JOIN FETCH up.account a WHERE a.username = :username")
    Optional<User> findByUsernameWithAccount(@Param("username") String username);

}