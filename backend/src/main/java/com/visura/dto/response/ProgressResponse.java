package com.visura.dto.response;

/**
 * Andamento da vistoria: ambientes ativos, os concluídos (com itens e todos avaliados), itens ativos e os avaliados
 * (com estado).
 */
public record ProgressResponse(
        long rooms,
        long completed,
        long items,
        long evaluated
) {

    public static final ProgressResponse NONE = new ProgressResponse(0, 0, 0, 0);
}
