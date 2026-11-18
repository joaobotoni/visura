package com.visura.storage;

import com.visura.exception.custom.StorageException;
import com.visura.exception.enums.problem.StorageProblem;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Arquivos das evidências num bucket compatível com S3 (AWS S3, Cloudflare R2, MinIO), sob um prefixo
 * ({@code evidencias/}). O nome guardado no banco é só o nome do arquivo, sem o prefixo, igual ao do disco: trocar o
 * armazenamento não muda os registros. A gravação usa {@code If-None-Match: *} para nunca sobrescrever um objeto.
 */
@Slf4j
public class ObjectStorage implements Storage {

    /** O nome gerado pela API: UUID e extensão. Qualquer outra coisa (barra, "..") é recusada antes de chegar ao bucket. */
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9-]+\\.[a-z0-9]+");
    private static final String NEW_OBJECT = "*";

    private final S3Client client;
    private final String bucket;
    private final String prefix;

    public ObjectStorage(S3Client client, String bucket, String prefix) {
        this.client = client;
        this.bucket = bucket;
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public void write(String name, byte[] content) {
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(key(name)).ifNoneMatch(NEW_OBJECT).build();
        try {
            client.putObject(request, RequestBody.fromBytes(content));
        } catch (SdkException exception) {
            throw new StorageException(StorageProblem.WRITE_FAILED, exception);
        }
    }

    @Override
    public byte[] read(String name) {
        GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key(name)).build();
        try {
            return client.getObjectAsBytes(request).asByteArray();
        } catch (SdkException exception) {
            throw new StorageException(StorageProblem.READ_FAILED, exception);
        }
    }

    @Override
    public void erase(String name) {
        try {
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key(name)).build());
        } catch (SdkException | StorageException exception) {
            log.warn("Arquivo de evidência não apagado: {}", name, exception);
        }
    }

    @Override
    public List<String> stale(Instant limit) {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build();
        try {
            return client.listObjectsV2Paginator(request).contents().stream()
                    .filter(object -> object.lastModified().isBefore(limit))
                    .map(this::name)
                    .filter(name -> NAME.matcher(name).matches())
                    .toList();
        } catch (SdkException exception) {
            throw new StorageException(StorageProblem.READ_FAILED, exception);
        }
    }

    private String key(String name) {
        if (name == null || !NAME.matcher(name).matches()) {
            throw new StorageException(StorageProblem.OUTSIDE_DIRECTORY);
        }
        return prefix + name;
    }

    private String name(S3Object object) {
        return object.key().substring(prefix.length());
    }
}
