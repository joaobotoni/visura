package com.visura.dto.response;

import com.visura.database.enums.PropertyCategory;
import com.visura.database.enums.PropertyType;

import java.util.UUID;

public record PropertyResponse(
        UUID id,
        String description,
        PropertyType type,
        PropertyCategory category,
        AddressResponse address,
        UUID company
) {
}
