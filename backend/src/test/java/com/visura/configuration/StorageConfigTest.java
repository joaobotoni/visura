package com.visura.configuration;

import com.visura.properties.BucketProperties;
import com.visura.properties.EvidenceProperties;
import com.visura.storage.DiskStorage;
import com.visura.storage.ObjectStorage;
import com.visura.storage.Storage;
import com.visura.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Escolha do armazenamento das evidências")
class StorageConfigTest {

    @Configuration
    @EnableConfigurationProperties({EvidenceProperties.class, BucketProperties.class})
    static class Properties {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Properties.class, StorageConfig.class)
            .withPropertyValues("vsr.evidence.orphan-age=1h", "vsr.evidence.bucket.region=us-east-1",
                    "vsr.evidence.bucket.prefix=evidencias/");

    @Test
    @Controle
    @DisplayName("sem EVIDENCE_STORAGE, usa o disco")
    void diskByDefault() {
        runner.withPropertyValues("vsr.evidence.directory=/tmp/visura-evidencias")
                .run(context -> assertThat(context.getBean(Storage.class)).isInstanceOf(DiskStorage.class));
    }

    @Test
    @Controle
    @DisplayName("com EVIDENCE_STORAGE=bucket, usa o bucket (inclusive com endpoint do R2 ou do MinIO)")
    void bucketWhenConfigured() {
        runner.withPropertyValues("vsr.evidence.storage=bucket", "vsr.evidence.bucket.name=visura",
                        "vsr.evidence.bucket.endpoint=http://localhost:9000", "vsr.evidence.bucket.path-style=true")
                .run(context -> assertThat(context.getBean(Storage.class)).isInstanceOf(ObjectStorage.class));
    }

    @Test
    @Controle
    @DisplayName("bucket sem nome, ou disco sem diretório, não inicia")
    void missingConfigurationFails() {
        runner.withPropertyValues("vsr.evidence.storage=bucket")
                .run(context -> assertThat(context).hasFailed());
        runner.run(context -> assertThat(context).hasFailed());
    }
}
