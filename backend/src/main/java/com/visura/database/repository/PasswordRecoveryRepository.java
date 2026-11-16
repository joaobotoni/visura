package com.visura.database.repository;

import com.visura.database.entity.PasswordRecovery;
import com.visura.vo.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecovery, Integer> {

    /** A recuperação aberta e dentro da validade, com o usuário. */
    @Query("select r from PasswordRecovery r join fetch r.user u join fetch u.person "
            + "where u.email = :email and r.closedAt is null and r.expiresAt > :now")
    Optional<PasswordRecovery> findOpen(@Param("email") Email email, @Param("now") Instant now);

    /** Encerra a recuperação aberta do usuário, antes de abrir outra. */
    @Modifying(flushAutomatically = true)
    @Query(value = "update usuarios.recuperacao_senha set encerrada_em = now() where id_usuario = :user and encerrada_em is null",
            nativeQuery = true)
    int close(@Param("user") Integer user);

    /** Gasta uma tentativa, só se ainda houver saldo; devolve 0 quando as tentativas acabaram ou já foi encerrada. */
    @Modifying
    @Query(value = "update usuarios.recuperacao_senha set tentativas = tentativas + 1 "
            + "where id_recuperacao = :recovery and encerrada_em is null and tentativas < :max", nativeQuery = true)
    int spend(@Param("recovery") Integer recovery, @Param("max") int max);

    /** Encerra a recuperação usada; devolve 0 se outro pedido a usou primeiro. */
    @Modifying
    @Query(value = "update usuarios.recuperacao_senha set encerrada_em = now() where id_recuperacao = :recovery and encerrada_em is null",
            nativeQuery = true)
    int redeem(@Param("recovery") Integer recovery);
}
