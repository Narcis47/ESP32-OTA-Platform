package com.narcis.esp32ota.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("programs")
public class Program {
    @Id
    private Long id;
    private Long boardId;
    private Long userId;
    private String name;
    private String code;
    private String status;
    private LocalDateTime createdAt;
}
