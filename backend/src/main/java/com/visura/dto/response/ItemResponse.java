package com.visura.dto.response;

import com.visura.database.enums.ItemCondition;

import java.util.UUID;

public record ItemResponse(
        UUID id,
        String name,
        UUID type,
        String description,
        ItemCondition condition,
        String material,
        String color,
        String notes,
        UUID origin
) {
}
