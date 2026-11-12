package com.visura.service;

import com.visura.database.entity.Inspection;
import com.visura.database.entity.Participant;
import com.visura.database.entity.Person;
import com.visura.database.repository.ParticipantRepository;
import com.visura.dto.request.ParticipantRequest;
import com.visura.dto.response.ParticipantResponse;
import com.visura.mapper.PersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Pessoas das vistorias do usuário (proprietário, inquilino), escolhidas no cadastro de pessoas dele. Com a vistoria
 * finalizada, o banco recusa a alteração.
 */
@Service
@RequiredArgsConstructor
public class ParticipantService {

    /** Teto das listas curtas, que não são paginadas. */
    private static final Limit LIST = Limit.of(200);

    private final ParticipantRepository participantRepository;
    private final InspectionService inspectionService;
    private final PersonService personService;
    private final PersonMapper personMapper;

    @Transactional(readOnly = true)
    public List<ParticipantResponse> list(UUID user, UUID inspection) {
        Inspection found = inspection(user, inspection);
        List<Participant> participants = participants(found);
        return show(participants);
    }

    @Transactional
    public void link(UUID user, UUID inspection, ParticipantRequest request) {
        Inspection found = inspection(user, inspection);
        Person person = person(user, request.person());
        bind(found, person, request);
    }

    @Transactional
    public void unlink(UUID user, UUID inspection, UUID person) {
        Inspection found = inspection(user, inspection);
        Person participant = person(user, person);
        release(found, participant);
    }

    private Inspection inspection(UUID user, UUID inspection) {
        return inspectionService.find(user, inspection);
    }

    private List<Participant> participants(Inspection inspection) {
        return participantRepository.findActive(inspection.getId(), LIST);
    }

    private List<ParticipantResponse> show(List<Participant> participants) {
        return participants.stream().map(personMapper::participant).toList();
    }

    private Person person(UUID user, UUID person) {
        return personService.find(user, person);
    }

    private void bind(Inspection inspection, Person person, ParticipantRequest request) {
        participantRepository.link(inspection.getId(), person.getId(), request.role().getValue());
    }

    private void release(Inspection inspection, Person person) {
        participantRepository.unlink(inspection.getId(), person.getId());
    }
}
