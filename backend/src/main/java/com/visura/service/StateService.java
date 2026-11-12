package com.visura.service;

import com.visura.database.entity.State;
import com.visura.database.repository.StateRepository;
import com.visura.dto.response.StateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Os 27 estados, carregados pela migration: a sigla é o que o endereço do imóvel recebe.
 */
@Service
@RequiredArgsConstructor
public class StateService {

    private final StateRepository stateRepository;

    @Transactional(readOnly = true)
    public List<StateResponse> list() {
        List<State> states = states();
        return show(states);
    }

    private List<State> states() {
        return stateRepository.findAllByOrderByAcronymAsc();
    }

    private static List<StateResponse> show(List<State> states) {
        return states.stream().map(state -> new StateResponse(state.getAcronym(), state.getName())).toList();
    }
}
