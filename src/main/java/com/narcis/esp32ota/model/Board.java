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
@Table("boards")
public class Board {
    @Id
    private Long id;
    private Long userId;
    private String name;
    private String chipModel;
    private Integer chipRevision;
    private Integer cpuFreqMhz;
    private Integer flashSize;
    private Integer heapSize;
    private String macAddress;
    private String status;
    private LocalDateTime lastSeen;
    private LocalDateTime createdAt;
}
