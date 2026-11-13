package com.visura.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Recuperação de senha por código enviado ao e-mail. Só o HMAC do código é gravado. As tentativas e o encerramento
 * são gravados por update condicional no banco, para que pedidos simultâneos não furem o limite.
 */
@Entity
@Getter
@Builder
@Table(schema = "usuarios", name = "recuperacao_senha")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordRecovery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recuperacao")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, updatable = false)
    private User user;

    @Column(name = "codigo_hash", nullable = false, updatable = false)
    private byte[] codeHash;

    @Column(name = "tentativas", insertable = false, updatable = false)
    private Integer attempts;

    @Column(name = "expira_em", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "encerrada_em", insertable = false, updatable = false)
    private Instant closedAt;
}
