package com.visura.database.converter;

import com.visura.vo.Phone;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PhoneConverter implements AttributeConverter<Phone, String> {

    @Override
    public String convertToDatabaseColumn(Phone phone) {
        if (phone == null) {
            return null;
        }
        return phone.value();
    }

    @Override
    public Phone convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return Phone.of(value);
    }
}
