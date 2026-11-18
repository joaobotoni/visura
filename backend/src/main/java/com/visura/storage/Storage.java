package com.visura.storage;

import java.time.Instant;
import java.util.List;

/**
 * Onde ficam os arquivos das evidências: em disco ({@link DiskStorage}) ou num bucket compatível com S3
 * ({@link ObjectStorage}). O nome do arquivo é sempre gerado pela API ({@code StorageService}); a implementação só
 * grava, lê, apaga e lista.
 */
public interface Storage {

    /** Grava o conteúdo com o nome dado; recusa sobrescrever um arquivo que já existe. */
    void write(String name, byte[] content);

    /** Lê o arquivo inteiro. */
    byte[] read(String name);

    /** Apaga o arquivo, se existir; a falha só vai para o log (o arquivo vira órfão e sai na limpeza). */
    void erase(String name);

    /** Nomes dos arquivos gravados antes de {@code limit}, candidatos à limpeza de órfãos. */
    List<String> stale(Instant limit);
}
