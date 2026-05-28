package com.narcis.esp32ota.service;

import com.narcis.esp32ota.model.Board;
import com.narcis.esp32ota.repository.BoardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public boolean registerBoard(Long userId, String name, String chipModel,
                                 Integer chipRevision, Integer cpuFreqMhz,
                                 Integer flashSize, Integer heapSize, String macAddress) {

        if (boardRepository.countByUserId(userId) >= 3) return false;

        Optional<Board> existing = boardRepository.findByMacAddress(macAddress);
        if (existing.isPresent()) {
            existing.get().setLastSeen(LocalDateTime.now());
            existing.get().setStatus("ONLINE");
            boardRepository.save(existing.get());
            return true;
        }

        Board board = Board.builder()
                .userId(userId)
                .name(name)
                .chipModel(chipModel)
                .chipRevision(chipRevision)
                .cpuFreqMhz(cpuFreqMhz)
                .flashSize(flashSize)
                .heapSize(heapSize)
                .macAddress(macAddress)
                .status("ONLINE")
                .lastSeen(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        boardRepository.save(board);
        return true;
    }

    public List<Board> getBoardsByUserId(Long userId){return boardRepository.findByUserId(userId);}

    public Optional<Board> getBoardById(Long id){return boardRepository.findById(id);}

    public boolean updateLastSeen(Long id){
        Optional<Board> board = boardRepository.findById(id);
        if (board.isPresent()){
            board.get().setLastSeen(LocalDateTime.now());
            boardRepository.save(board.get());
            return true;
        }
        return false;
    }

    public boolean updateStatus(Long id, String status){
        Optional<Board> board = boardRepository.findById(id);
        if (board.isPresent()){
            board.get().setStatus(status);
            boardRepository.save(board.get());
            return true;
        }
        return false;
    }

    public boolean deleteBoard(Long id){
        Optional<Board> board = boardRepository.findById(id);
        if (board.isPresent()){
            boardRepository.delete(board.get());
            return true;
        }
        return false;
    }
}
