package com.visura.storage;

import com.visura.exception.custom.StorageException;
import com.visura.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Evidências em disco")
class DiskStorageTest {

    @TempDir
    private Path directory;

    @Test
    @Controle
    @DisplayName("lê o que gravou")
    void readsWhatItWrote() {
        DiskStorage storage = new DiskStorage(directory);

        storage.write("a.jpg", new byte[]{1, 2, 3});

        assertThat(storage.read("a.jpg")).containsExactly(1, 2, 3);
    }

    @Test
    @Controle
    @DisplayName("não sobrescreve um arquivo que já existe")
    void refusesOverwrite() {
        DiskStorage storage = new DiskStorage(directory);
        storage.write("a.jpg", new byte[]{1});

        assertThatThrownBy(() -> storage.write("a.jpg", new byte[]{2})).isInstanceOf(StorageException.class);
        assertThat(storage.read("a.jpg")).containsExactly(1);
    }

    @Test
    @Controle
    @DisplayName("não lê fora do diretório")
    void refusesOutsideDirectory() throws Exception {
        Path inner = Files.createDirectory(directory.resolve("evidencias"));
        Files.write(directory.resolve("segredo.txt"), new byte[]{9});
        DiskStorage storage = new DiskStorage(inner);

        assertThatThrownBy(() -> storage.read("../segredo.txt")).isInstanceOf(StorageException.class);
    }

    @Test
    @Controle
    @DisplayName("apagar um arquivo que não existe não falha")
    void eraseMissingFile() {
        assertThatCode(() -> new DiskStorage(directory).erase("nada.jpg")).doesNotThrowAnyException();
    }

    @Test
    @Controle
    @DisplayName("lista só os arquivos anteriores ao limite, e nada se o diretório ainda não existe")
    void staleListsOldFiles() throws Exception {
        Instant limit = Instant.parse("2026-11-18T12:00:00Z");
        Files.setLastModifiedTime(Files.write(directory.resolve("velho.jpg"), new byte[]{1}),
                FileTime.from(limit.minus(Duration.ofHours(2))));
        Files.setLastModifiedTime(Files.write(directory.resolve("novo.jpg"), new byte[]{1}),
                FileTime.from(limit.plus(Duration.ofMinutes(5))));

        assertThat(new DiskStorage(directory).stale(limit)).containsExactly("velho.jpg");
        assertThat(new DiskStorage(directory.resolve("ainda-nao-existe")).stale(limit)).isEmpty();
    }

    @Test
    @Controle
    @DisplayName("sem EVIDENCE_DIR, não inicia")
    void requiresDirectory() {
        assertThatThrownBy(() -> new DiskStorage(null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new DiskStorage(Path.of(""))).isInstanceOf(IllegalStateException.class);
    }
}
