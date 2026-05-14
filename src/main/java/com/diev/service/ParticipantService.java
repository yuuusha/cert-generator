package com.diev.service;

import com.diev.entity.Participant;
import com.diev.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    @Transactional
    public Participant saveParticipant(Participant participant) {
        validateParticipant(participant);
        
        Long participantId = participantRepository.addParticipant(participant);
        participant.setId(participantId);
        
        return participant;
    }

    private void validateParticipant(Participant participant) {
        if (participant.getFullName() == null || participant.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        
        if (participant.getUniversity() == null || participant.getUniversity().trim().isEmpty()) {
            throw new IllegalArgumentException("University is required");
        }
        
        if (participant.getCity() == null || participant.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        
        if (participant.getFullName().length() > 200) {
            throw new IllegalArgumentException("Full name cannot exceed 200 characters");
        }
        
        if (participant.getUniversity().length() > 200) {
            throw new IllegalArgumentException("University name cannot exceed 200 characters");
        }
        
        if (participant.getCity().length() > 100) {
            throw new IllegalArgumentException("City name cannot exceed 100 characters");
        }
        
        if (participant.getFaculty() != null && participant.getFaculty().length() > 200) {
            throw new IllegalArgumentException("Faculty name cannot exceed 200 characters");
        }
    }

    public Participant findParticipant(Long id) {
        return participantRepository.findParticipant(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with id: " + id));
    }

    public List<Participant> getAllParticipants() {
        return participantRepository.getAllParticipants();
    }
}
