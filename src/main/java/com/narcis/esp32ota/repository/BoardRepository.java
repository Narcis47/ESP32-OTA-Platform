package com.narcis.esp32ota.repository;

import com.narcis.esp32ota.model.Board;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends CrudRepository<Board, Long> {
    List<Board> findByUserId (Long userId);
    Optional<Board> findByMacAddress (String macAddress);
    int countByUserId (Long userId);
}
