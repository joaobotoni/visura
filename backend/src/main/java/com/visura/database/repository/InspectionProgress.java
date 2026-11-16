package com.visura.database.repository;

/**
 * Projeção da contagem de ambientes e itens de uma vistoria ({@link InspectionRepository#progress}).
 */
public interface InspectionProgress {

    Integer getInspection();

    Long getRooms();

    Long getCompleted();

    Long getItems();

    Long getEvaluated();
}
