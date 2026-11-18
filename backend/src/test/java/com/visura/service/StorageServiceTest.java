package com.visura.service;

import com.visura.storage.Storage;
import com.visura.support.Controle;
import com.visura.vo.Photo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Arquivos das evidências")
class StorageServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3};

    private final Storage storage = mock(Storage.class);
    private final StorageService service = new StorageService(storage, Clock.fixed(NOW, ZoneOffset.UTC));

    @AfterEach
    void clear() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @Controle
    @DisplayName("grava com nome gerado (UUID e extensão do formato real) e devolve o nome")
    void saveGeneratesName() {
        String name = service.save(Photo.of(JPEG));

        assertThat(name).matches("[0-9a-f-]{36}\\.jpg");
        verify(storage).write(name, JPEG);
    }

    @Test
    @Controle
    @DisplayName("apaga o arquivo quando a transação que o gravou é desfeita")
    void rollbackErasesFile() {
        TransactionSynchronizationManager.initSynchronization();

        String name = service.save(Photo.of(JPEG));
        complete(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(storage).erase(name);
    }

    @Test
    @Controle
    @DisplayName("mantém o arquivo quando a transação é confirmada")
    void commitKeepsFile() {
        TransactionSynchronizationManager.initSynchronization();

        service.save(Photo.of(JPEG));
        complete(TransactionSynchronization.STATUS_COMMITTED);

        verify(storage, never()).erase(anyString());
    }

    @Test
    @Controle
    @DisplayName("os órfãos candidatos são os gravados antes de agora menos a idade")
    void staleUsesAge() {
        when(storage.stale(any())).thenReturn(List.of("velho.jpg"));

        assertThat(service.stale(Duration.ofHours(1))).containsExactly("velho.jpg");
        verify(storage).stale(NOW.minus(Duration.ofHours(1)));
    }

    private static void complete(int status) {
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(status));
    }
}
