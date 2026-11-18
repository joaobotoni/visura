package com.visura.service;

import com.visura.storage.Storage;
import com.visura.vo.Photo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Arquivos das evidências, em disco ou num bucket ({@link Storage}, escolhido por {@code vsr.evidence.storage}). O nome
 * é sempre gerado aqui (UUID + extensão do formato real). Gravado dentro de uma transação que depois é desfeita, o
 * arquivo é apagado.
 */
@Service
@RequiredArgsConstructor
public class StorageService {

    private final Storage storage;
    private final Clock clock;

    public String save(Photo photo) {
        String name = name(photo);
        write(name, photo);
        cleanup(name);
        return name;
    }

    public byte[] load(String name) {
        return read(name);
    }

    /** Apaga o arquivo, se existir; a falha só vai para o log (o arquivo vira órfão e sai na limpeza). */
    public void delete(String name) {
        erase(name);
    }

    /** Nomes dos arquivos mais antigos que {@code age}, candidatos à limpeza de órfãos. */
    public List<String> stale(Duration age) {
        Instant limit = limit(age);
        return older(limit);
    }

    private static String name(Photo photo) {
        return UUID.randomUUID() + photo.extension();
    }

    private void write(String name, Photo photo) {
        storage.write(name, photo.content());
    }

    private byte[] read(String name) {
        return storage.read(name);
    }

    private void erase(String name) {
        storage.erase(name);
    }

    private Instant limit(Duration age) {
        return clock.instant().minus(age);
    }

    private List<String> older(Instant limit) {
        return storage.stale(limit);
    }

    private void cleanup(String name) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new Cleanup(storage, name));
    }

    /** Apaga o arquivo gravado quando a transação que o registrou é desfeita. */
    private record Cleanup(Storage storage, String name) implements TransactionSynchronization {

        @Override
        public void afterCompletion(int status) {
            if (status != STATUS_ROLLED_BACK) {
                return;
            }
            storage.erase(name);
        }
    }
}
