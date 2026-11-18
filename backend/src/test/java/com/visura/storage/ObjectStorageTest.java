package com.visura.storage;

import com.visura.exception.custom.StorageException;
import com.visura.exception.enums.problem.StorageProblem;
import com.visura.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Evidências num bucket compatível com S3")
class ObjectStorageTest {

    private static final String NAME = "0b6f1c1e-7c1a-4a43-9a51-2f4f0e1d2c3b.jpg";
    private static final Instant LIMIT = Instant.parse("2026-11-18T12:00:00Z");

    private final S3Client client = mock(S3Client.class);
    private final ObjectStorage storage = new ObjectStorage(client, "visura", "evidencias/");

    @Test
    @Controle
    @DisplayName("grava no bucket, sob o prefixo, sem sobrescrever objeto existente")
    void writeUsesPrefixAndNeverOverwrites() {
        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);

        storage.write(NAME, new byte[]{1, 2});

        verify(client).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().bucket()).isEqualTo("visura");
        assertThat(request.getValue().key()).isEqualTo("evidencias/" + NAME);
        assertThat(request.getValue().ifNoneMatch()).isEqualTo("*");
    }

    @Test
    @Controle
    @DisplayName("falha do bucket na gravação vira erro de gravação")
    void writeFailure() {
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().statusCode(412).message("existe").build());

        assertThatThrownBy(() -> storage.write(NAME, new byte[]{1}))
                .isInstanceOfSatisfying(StorageException.class,
                        exception -> assertThat(exception.problem()).isEqualTo(StorageProblem.WRITE_FAILED));
    }

    @Test
    @Controle
    @DisplayName("lê o objeto do prefixo; objeto ausente vira erro de leitura")
    void read() {
        when(client.getObjectAsBytes(GetObjectRequest.builder().bucket("visura").key("evidencias/" + NAME).build()))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[]{7}));

        assertThat(storage.read(NAME)).containsExactly(7);

        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertThatThrownBy(() -> storage.read(NAME))
                .isInstanceOfSatisfying(StorageException.class,
                        exception -> assertThat(exception.problem()).isEqualTo(StorageProblem.READ_FAILED));
    }

    @Test
    @Controle
    @DisplayName("recusa nome que não foi gerado pela API (barra, \"..\") antes de chegar ao bucket")
    void refusesForeignNames() {
        assertThatThrownBy(() -> storage.read("../outro/arquivo.jpg"))
                .isInstanceOfSatisfying(StorageException.class,
                        exception -> assertThat(exception.problem()).isEqualTo(StorageProblem.OUTSIDE_DIRECTORY));
        assertThatThrownBy(() -> storage.write("a/b.jpg", new byte[]{1})).isInstanceOf(StorageException.class);
        verifyNoInteractions(client);
    }

    @Test
    @Controle
    @DisplayName("apagar só registra a falha no log")
    void eraseOnlyLogs() {
        when(client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(S3Exception.builder().statusCode(500).build());

        assertThatCode(() -> storage.erase(NAME)).doesNotThrowAnyException();
        assertThatCode(() -> storage.erase("../fora.jpg")).doesNotThrowAnyException();
    }

    @Test
    @Controle
    @DisplayName("lista, sem o prefixo, só os objetos anteriores ao limite e com nome gerado pela API")
    void staleListsOldObjects() {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket("visura").prefix("evidencias/").build();
        ListObjectsV2Response page = ListObjectsV2Response.builder().contents(
                object("evidencias/" + NAME, LIMIT.minus(Duration.ofHours(2))),
                object("evidencias/0b6f1c1e-7c1a-4a43-9a51-000000000000.png", LIMIT.plus(Duration.ofMinutes(1))),
                object("evidencias/pasta/estranho.jpg", LIMIT.minus(Duration.ofHours(2)))).build();
        when(client.listObjectsV2Paginator(request)).thenReturn(new ListObjectsV2Iterable(client, request));
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(page);

        assertThat(storage.stale(LIMIT)).containsExactly(NAME);
    }

    private static S3Object object(String key, Instant modified) {
        return S3Object.builder().key(key).lastModified(modified).build();
    }
}
