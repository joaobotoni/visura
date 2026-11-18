import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Configuração local (app/local.properties, fora do Git) com a variável de ambiente como alternativa:
// visura.api.url -> VISURA_API_URL, visura.release.store.file -> VISURA_RELEASE_STORE_FILE etc.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun config(nome: String): String? =
    localProperties.getProperty(nome)?.takeIf { it.isNotBlank() }
        ?: providers.environmentVariable(nome.uppercase().replace('.', '_')).orNull?.takeIf { it.isNotBlank() }

// Endereço de produção da API: obrigatório e com HTTPS para gerar o release.
val urlRelease = config("visura.api.url.release")
val verificarRelease = tasks.register("verificarConfiguracaoRelease") {
    val url = urlRelease
    doLast {
        require(url != null && url.startsWith("https://") && url.endsWith("/api/1/")) {
            "Defina visura.api.url.release (ou VISURA_API_URL_RELEASE) com o endereço HTTPS da API, " +
                "terminado em /api/1/, para gerar o release. Atual: ${url ?: "não definido"}"
        }
    }
}
tasks.configureEach {
    if (name == "preReleaseBuild") dependsOn(verificarRelease)
}

android {
    namespace = "com.visura"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.visura"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Endereço da API (backend/) no debug e na homologação. O padrão é o localhost do computador
        // visto pelo emulador. Num aparelho físico, defina visura.api.url (ex.: http://192.168.0.10:8080/api/1/).
        buildConfigField("String", "API_BASE_URL", "\"${config("visura.api.url") ?: "http://10.0.2.2:8080/api/1/"}\"")

        // Login com Google: o client ID do tipo "Web application" do projeto no Google Cloud, o mesmo
        // configurado na API (GOOGLE_CLIENT_ID). Vazio desliga o botão.
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${config("visura.google.client.id").orEmpty()}\"")
    }

    // Assinatura do release: o keystore fica fora do repositório e é informado pelo local.properties
    // ou por variáveis de ambiente (CI). Sem ele, o release sai sem assinatura.
    val keystoreRelease = config("visura.release.store.file")
    signingConfigs {
        if (keystoreRelease != null) {
            create("release") {
                storeFile = file(keystoreRelease)
                storePassword = config("visura.release.store.password")
                keyAlias = config("visura.release.key.alias")
                keyPassword = config("visura.release.key.password")
            }
        }
    }

    buildTypes {
        release {
            // R8 reduz e ofusca o código; as regras do Gson ficam em src/main/keepRules/rules.keep.
            optimization {
                enable = true
            }
            // Produção só fala HTTPS (o HTTP sem TLS só é liberado no debug e na homologação).
            buildConfigField("String", "API_BASE_URL", "\"${urlRelease.orEmpty()}\"")
            if (keystoreRelease != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        // Igual ao release (R8 ligado), mas assinado com a chave de debug, com HTTP liberado
        // (src/homologacao/AndroidManifest.xml) e apontando para visura.api.url: serve para testar
        // o build otimizado contra a API local antes de publicar.
        create("homologacao") {
            initWith(getByName("release"))
            buildConfigField("String", "API_BASE_URL", "\"${config("visura.api.url") ?: "http://10.0.2.2:8080/api/1/"}\"")
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.work.runtime.ktx)
}