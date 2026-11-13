package com.visura.database.repository;

import com.visura.database.entity.User;
import com.visura.vo.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u from User u join fetch u.person where u.uuid = :uuid")
    Optional<User> findWithPersonByUuid(@Param("uuid") UUID uuid);

    @Query("select u from User u join fetch u.person where u.email = :email")
    Optional<User> findWithPersonByEmail(@Param("email") Email email);

    /** O usuário que já vinculou esta conta do Google ({@code sub} do ID token). */
    @Query(value = "select u.uuid from usuarios.usuario u join usuarios.credencial_social c on c.id_usuario = u.id_usuario "
            + "where c.provedor = 'google' and c.identificador_externo = :subject", nativeQuery = true)
    Optional<UUID> findGoogle(@Param("subject") String subject);

    /** Vincula a conta do Google ao usuário; vincular de novo não muda nada. */
    @Transactional
    @Modifying
    @Query(value = "insert into usuarios.credencial_social (id_usuario, provedor, identificador_externo) "
            + "values (:user, 'google', :subject) on conflict do nothing", nativeQuery = true)
    int linkGoogle(@Param("user") Integer user, @Param("subject") String subject);
}
