package com.visura.storage;

import com.visura.exception.custom.StorageException;
import com.visura.exception.enums.problem.StorageProblem;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Arquivos das evidências num diretório local ({@code EVIDENCE_DIR}). A leitura confere o caminho real dentro do
 * diretório e não segue link simbólico.
 */
@Slf4j
public class DiskStorage implements Storage {

    private final Path directory;

    public DiskStorage(Path directory) {
        this.directory = configured(directory).toAbsolutePath().normalize();
    }

    @Override
    public void write(String name, byte[] content) {
        try {
            Files.createDirectories(directory);
            Files.write(path(name), content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new StorageException(StorageProblem.WRITE_FAILED, exception);
        }
    }

    @Override
    public byte[] read(String name) {
        Path real = real(path(name));
        inside(real);
        try (InputStream input = Files.newInputStream(real, LinkOption.NOFOLLOW_LINKS)) {
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new StorageException(StorageProblem.READ_FAILED, exception);
        }
    }

    @Override
    public void erase(String name) {
        try {
            Files.deleteIfExists(path(name));
        } catch (IOException exception) {
            log.warn("Arquivo de evidência não apagado: {}", name, exception);
        }
    }

    @Override
    public List<String> stale(Instant limit) {
        return list().stream().filter(path -> older(path, limit)).map(path -> path.getFileName().toString()).toList();
    }

    private Path path(String name) {
        return directory.resolve(name).normalize();
    }

    private List<Path> list() {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)).toList();
        } catch (IOException exception) {
            throw new StorageException(StorageProblem.READ_FAILED, exception);
        }
    }

    private void inside(Path real) {
        if (!real.startsWith(real(directory))) {
            throw new StorageException(StorageProblem.OUTSIDE_DIRECTORY);
        }
    }

    private static Path real(Path path) {
        try {
            return path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException exception) {
            throw new StorageException(StorageProblem.READ_FAILED, exception);
        }
    }

    private static Path configured(Path directory) {
        if (directory == null || directory.toString().isBlank()) {
            throw new IllegalStateException("Defina EVIDENCE_DIR para guardar as evidências em disco.");
        }
        return directory;
    }

    private static boolean older(Path path, Instant limit) {
        try {
            return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toInstant().isBefore(limit);
        } catch (IOException exception) {
            return false;
        }
    }
}
