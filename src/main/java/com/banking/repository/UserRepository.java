package com.banking.repository;

import com.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByAccountNumber(String accountNumber);

    Optional<User> findByUserId(Long userId);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.accountNumber = :accountNumber AND u.isActive = true")
    Optional<User> findActiveUserByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'BANNED'")
    List<User> findAllBannedUsers();

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'SUSPENDED'")
    List<User> findAllSuspendedUsers();
}
