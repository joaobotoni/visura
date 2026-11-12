package com.visura.mapper;

import com.visura.database.entity.Individual;
import com.visura.vo.Cpf;
import com.visura.vo.Name;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface IndividualMapper {

    /** Pessoa de uma conta: sem titular. */
    default Individual entity(Name name, Cpf cpf) {
        return new Individual(null, name, cpf);
    }
}
