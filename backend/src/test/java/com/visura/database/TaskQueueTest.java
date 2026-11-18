package com.visura.database;

import com.visura.support.Controle;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A fila de tarefas direto no PostgreSQL: reserva sem disputa entre instâncias, tentativas, espera e abandono.
 */
@DisplayName("Fila de tarefas no banco")
class TaskQueueTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final List<String> FILES = List.of("V1__schema.sql", "V2__functions.sql", "V3__ddl.sql",
            "V4__procedures.sql", "V5__triggers.sql", "V6__initial_data.sql", "V8__imoveis.sql", "V9__vistorias.sql",
            "V10__evidencias.sql", "V11__pessoas.sql", "V12__empresas_catalogo_enderecos.sql",
            "V13__regras_integridade_auditoria.sql", "V14__recuperacao_senha_cor_item.sql", "V15__fila_tarefas.sql");
    private static final String REFERENCE = "'00000000-0000-0000-0000-00000000a001'";

    private static EmbeddedPostgres postgres;
    private static Connection connection;

    @BeforeAll
    static void migrate() throws Exception {
        postgres = EmbeddedPostgres.start();
        connection = postgres.getPostgresDatabase().getConnection();
        for (String file : FILES) {
            execute(connection, Files.readString(MIGRATIONS.resolve(file)).replace("create extension if not exists pg_cron;", ""));
        }
    }

    @AfterAll
    static void stop() throws Exception {
        connection.close();
        postgres.close();
    }

    /** Cada teste começa com a fila vazia (o dono das migrations pode apagar; a aplicação não). */
    @BeforeEach
    void empty() throws SQLException {
        execute(connection, "delete from processamento.tarefa");
    }

    @Test
    @Controle
    @DisplayName("reserva a mais antiga, conta a tentativa e marca como em execução")
    void reservesOldestFirst() throws SQLException {
        String first = enqueue();
        enqueue();

        assertThat(text("select uuid::text from processamento.reservar_tarefa(900)")).isEqualTo(first);
        assertThat(text("select status || ':' || tentativas from processamento.tarefa where uuid = '" + first + "'"))
                .isEqualTo("executando:1");
    }

    @Test
    @Controle
    @DisplayName("tarefa travada por outra instância é pulada, sem esperar")
    void skipsLockedTask() throws Exception {
        String first = enqueue();
        String second = enqueue();
        try (Connection other = postgres.getPostgresDatabase().getConnection()) {
            other.setAutoCommit(false);
            execute(other, "select id_tarefa from processamento.tarefa where uuid = '" + first + "' for update");

            assertThat(text("select uuid::text from processamento.reservar_tarefa(900)")).isEqualTo(second);
            other.rollback();
        }
    }

    @Test
    @Controle
    @DisplayName("concluída sai da fila")
    void concludedLeavesQueue() throws SQLException {
        String task = enqueue();
        execute(connection, "select 1 from processamento.reservar_tarefa(900)");

        execute(connection, "call processamento.concluir_tarefa(" + id(task) + ")");

        assertThat(text("select status::text from processamento.tarefa where uuid = '" + task + "'")).isEqualTo("concluida");
        assertThat(count("select count(*) from processamento.reservar_tarefa(900)")).isZero();
    }

    @Test
    @Controle
    @DisplayName("adiada volta à fila só depois da espera, com o erro guardado")
    void postponedWaits() throws SQLException {
        String task = enqueue();
        execute(connection, "select 1 from processamento.reservar_tarefa(900)");

        execute(connection, "call processamento.adiar_tarefa(" + id(task) + ", 'sem rede', 60)");

        assertThat(text("select status || ':' || erro from processamento.tarefa where uuid = '" + task + "'")).isEqualTo("pendente:sem rede");
        assertThat(count("select count(*) from processamento.reservar_tarefa(900)")).isZero();
        execute(connection, "update processamento.tarefa set disponivel_em = now() - interval '1 second'");
        assertThat(text("select uuid::text from processamento.reservar_tarefa(900)")).isEqualTo(task);
    }

    @Test
    @Controle
    @DisplayName("sem tentativas restantes, a falha é definitiva")
    void exhaustedFails() throws SQLException {
        String task = enqueue();
        execute(connection, "update processamento.tarefa set maximo_tentativas = 1");
        execute(connection, "select 1 from processamento.reservar_tarefa(900)");

        execute(connection, "call processamento.adiar_tarefa(" + id(task) + ", 'erro', 0)");

        assertThat(text("select status::text from processamento.tarefa where uuid = '" + task + "'")).isEqualTo("falhou");
        assertThat(count("select count(*) from processamento.reservar_tarefa(900)")).isZero();
    }

    @Test
    @Controle
    @DisplayName("em execução além do prazo (instância caiu) é reservada de novo; sem tentativas, falha")
    void abandonedTaskIsRetaken() throws SQLException {
        String task = enqueue();
        execute(connection, "select 1 from processamento.reservar_tarefa(900)");
        execute(connection, "update processamento.tarefa set iniciada_em = now() - interval '20 minutes'");

        assertThat(text("select uuid::text || ':' || tentativas from processamento.reservar_tarefa(900)")).isEqualTo(task + ":2");

        execute(connection, "update processamento.tarefa set iniciada_em = now() - interval '20 minutes', maximo_tentativas = 2");
        assertThat(count("select count(*) from processamento.reservar_tarefa(900)")).isZero();
        assertThat(text("select status::text from processamento.tarefa where uuid = '" + task + "'")).isEqualTo("falhou");
    }

    @Test
    @Controle
    @DisplayName("tipo fora do padrão (maiúscula, espaço) é recusado")
    void typeIsValidated() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> execute(connection, "select processamento.enfileirar_tarefa('Transcrição', " + REFERENCE + ")"))
                .isInstanceOf(SQLException.class);
    }

    private static String enqueue() throws SQLException {
        return text("select processamento.enfileirar_tarefa('transcricao', " + REFERENCE + ")::text");
    }

    private static String id(String task) throws SQLException {
        return text("select id_tarefa::text from processamento.tarefa where uuid = '" + task + "'");
    }

    private static int count(String sql) throws SQLException {
        return Integer.parseInt(text(sql));
    }

    private static String text(String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getString(1);
        }
    }

    private static void execute(Connection target, String sql) throws SQLException {
        try (Statement statement = target.createStatement()) {
            statement.execute(sql);
        }
    }
}
