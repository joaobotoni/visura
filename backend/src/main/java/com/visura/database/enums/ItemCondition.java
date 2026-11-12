package com.visura.database.enums;

import com.visura.exception.custom.InspectionException;
import com.visura.exception.enums.problem.InspectionProblem;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum ItemCondition {

    NEW("novo"),
    EXCELLENT("excelente"),
    GOOD("bom"),
    FAIR("regular"),
    POOR("ruim"),
    DAMAGED("danificado");

    @JsonValue
    private final String value;

    ItemCondition(String value) {
        this.value = value;
    }

    public static ItemCondition from(String value) {
        for (ItemCondition condition : values()) {
            if (condition.value.equals(value)) {
                return condition;
            }
        }
        throw new InspectionException(InspectionProblem.UNKNOWN_CONDITION, value);
    }
}
