package com.visura.configuration;

import com.visura.properties.BucketProperties;
import com.visura.properties.EvidenceProperties;
import com.visura.storage.DiskStorage;
import com.visura.storage.ObjectStorage;
import com.visura.storage.Storage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * Onde ficam os arquivos das evidências, por {@code EVIDENCE_STORAGE}: {@code disk} (padrão, {@code EVIDENCE_DIR}) ou
 * {@code bucket} (S3, Cloudflare R2 ou MinIO, {@code EVIDENCE_BUCKET*}).
 */
@Configuration
public class StorageConfig {

    private static final String MODE = "vsr.evidence.storage";

    @Bean
    @ConditionalOnProperty(name = MODE, havingValue = "disk", matchIfMissing = true)
    public Storage diskStorage(EvidenceProperties properties) {
        return new DiskStorage(properties.directory());
    }

    @Bean
    @ConditionalOnProperty(name = MODE, havingValue = "bucket")
    public Storage objectStorage(S3Client evidenceClient, BucketProperties properties) {
        return new ObjectStorage(evidenceClient, required(properties.name()), properties.prefix());
    }

    /**
     * Os checksums só quando a operação exige: os provedores compatíveis com S3 nem sempre aceitam os checksums que o
     * SDK manda por padrão.
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = MODE, havingValue = "bucket")
    public S3Client evidenceClient(BucketProperties properties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .forcePathStyle(properties.pathStyle())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .credentialsProvider(DefaultCredentialsProvider.builder().build());
        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }
        return builder.build();
    }

    private static String required(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("Defina EVIDENCE_BUCKET para guardar as evidências num bucket.");
        }
        return bucket;
    }
}
