package com.narcis.esp32ota.service;

import com.narcis.esp32ota.model.Program;
import com.narcis.esp32ota.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProgramService {
    private final ProgramRepository programRepository;

    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public boolean uploadProgram(Long boardId, Long userId, String name, String code) {
        Program program = Program.builder()
                .boardId(boardId)
                .userId(userId)
                .name(name)
                .code(code)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        programRepository.save(program);
        return true;
    }

    public Optional<Program> getPendingProgram(Long boardId) {
        return programRepository.findByBoardId(boardId)
                .stream()
                .filter(p -> !p.getStatus().equals("DONE") && !p.getStatus().equals("FAILED"))
                .findFirst();
    }
    public Optional<Program> getProgramById(Long id) {return programRepository.findById(id);}

    public List<Program> getProgramsByBoardId(Long boardId){return programRepository.findByBoardId(boardId);}

    public List<Program> getProgramsByUserId(Long userId){return programRepository.findByUserId(userId);}

    public Optional<Program> getLatestProgramByBoardId(Long boardId) {return programRepository.findTopByBoardIdOrderByCreatedAtDesc(boardId);}

    public boolean updateProgramStatus(Long id, String status){
        Optional<Program> program = programRepository.findById(id);
        if (program.isPresent()){
            program.get().setStatus(status);
            programRepository.save(program.get());
            return true;
        }
        return false;
    }

    public boolean deleteProgram(Long id){
        Optional<Program> program = programRepository.findById(id);
        if (program.isPresent()){
            programRepository.delete(program.get());
            return true;
        }
        return false;
    }
}
