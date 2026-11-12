package com.visura.database.converter;

import com.visura.vo.Cpf;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CpfConverter implements AttributeConverter<Cpf, String> {

    @Override
    public String convertToDatabaseColumn(Cpf cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.value();
    }

    @Override
    public Cpf convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return Cpf.of(value);
    }
}
