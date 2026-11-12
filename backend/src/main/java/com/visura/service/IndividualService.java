package com.visura.service;

import com.visura.database.entity.Individual;
import com.visura.database.repository.IndividualRepository;
import com.visura.mapper.IndividualMapper;
import com.visura.vo.Cpf;
import com.visura.vo.Name;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndividualService {

    private final IndividualRepository individualRepository;
    private final IndividualMapper individualMapper;

    @Transactional
    public Individual save(Name name, Cpf cpf) {
        return individualRepository.save(create(name, cpf));
    }

    private Individual create(Name name, Cpf cpf) {
        return individualMapper.entity(name, cpf);
    }
}