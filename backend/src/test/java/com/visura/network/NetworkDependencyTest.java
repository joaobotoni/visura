package com.visura.network;

import com.visura.support.Controle;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@DisplayName("Dependências do pacote de rede")
class NetworkDependencyTest {

    private static final String FACADE = "ClientNetwork";

    private static final JavaClasses NETWORK = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.visura.network");

    @Test
    @Controle
    @DisplayName("o núcleo da rede só depende do JDK e de si mesmo: nada de HTTP, exceções da API ou Spring")
    void coreDependsOnlyOnJdk() {
        classes().that().resideInAPackage("com.visura.network..").and().doNotHaveSimpleName(FACADE)
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "com.visura.network..")
                .check(NETWORK);
    }

    @Test
    @Controle
    @DisplayName("a fachada só acrescenta as exceções da API, para recusar endereço inválido com 400")
    void facadeAddsOnlyApiExceptions() {
        classes().that().haveSimpleName(FACADE)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java..", "com.visura.network..", "com.visura.exception..")
                .check(NETWORK);
    }
}
