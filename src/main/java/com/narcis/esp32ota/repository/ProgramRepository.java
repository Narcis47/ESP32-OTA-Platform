package com.narcis.esp32ota.repository;

import com.narcis.esp32ota.model.Program;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends CrudRepository<Program, Long> {
    List<Program> findByBoardId (Long boardId);
    List<Program> findByUserId (Long userId);
    Optional<Program> findByBoardIdAndStatus (Long boardId, String status);
    Optional<Program> findTopByBoardIdOrderByCreatedAtDesc(Long boardId);
}
