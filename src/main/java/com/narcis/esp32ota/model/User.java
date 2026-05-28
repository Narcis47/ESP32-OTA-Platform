package com.narcis.esp32ota.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
    @JsonIgnore
    private String passwordHash;
    private String apiToken;
    private Boolean verified;
    @JsonIgnore
    private String verificationToken;
    private LocalDateTime createdAt;
}
