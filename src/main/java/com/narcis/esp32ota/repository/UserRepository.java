package com.narcis.esp32ota.repository;

import com.narcis.esp32ota.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail (String email);
    Optional<User> findByUsername (String username);
    Optional<User> findByApiToken (String apiToken);
    Optional<User> findByVerificationToken (String verificationToken);
    boolean existsByEmail (String email);
}
