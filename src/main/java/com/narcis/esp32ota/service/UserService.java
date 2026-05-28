package com.narcis.esp32ota.service;

import com.narcis.esp32ota.model.User;
import com.narcis.esp32ota.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final List<String> DISPOSABLE_DOMAINS = List.of(
            "10minutemail.com", "tempmail.com", "guerrillamail.com",
            "mailinator.com", "throwaway.email", "yopmail.com",
            "sharklasers.com", "guerrillamailblock.com", "grr.la",
            "spam4.me", "trashmail.com", "dispostable.com"
    );


    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean register(String username, String email, String password){
        if(userRepository.existsByEmail(email)) {return false;}
        if(isDisposableEmail(email)) return false;

        String passwordHash = passwordEncoder.encode(password);
        String apiToken = UUID.randomUUID().toString();
        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .apiToken(apiToken)
                .verified(false)
                .verificationToken(verificationToken)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        // Email verification -- After email service;

        return true;
    }

   public Optional<User> login(String email, String passwordHash){
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(passwordHash, user.get().getPasswordHash())){
            return user;
        }
        return Optional.empty();
   }

    public Optional<User> getUserById(Long id) {return userRepository.findById(id);}

    public Optional<User> getUserByApiToken(String apiToken) {return userRepository.findByApiToken(apiToken);}

    public boolean regenerateApiToken(Long id){
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()){
            String apiToken = UUID.randomUUID().toString();
            user.get().setApiToken(apiToken);
            userRepository.save(user.get());
            return true;
        }
        return false;
    }

    public boolean verifyEmail(String verificationToken){
        Optional<User> user = userRepository.findByVerificationToken(verificationToken);
        if (user.isPresent()){
            user.get().setVerified(true);
            user.get().setVerificationToken(null);
            userRepository.save(user.get());
            return true;
        }
        return false;
    }

    public boolean isDisposableEmail(String email){
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        return DISPOSABLE_DOMAINS.contains(domain);
    }

}
