package com.visura.database.repository;

import com.visura.database.entity.Inspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Toda consulta filtra pelo acesso (responsável ou membro da empresa da vistoria) e ignora as vistorias removidas.
 */
public interface InspectionRepository extends JpaRepository<Inspection, Integer> {

    String OWNED = "where " + Access.INSPECTION + "and i.removedAt is null";

    String FILTERED = OWNED + " and (:property is null or p.uuid = :property) and (:status is null or cast(i.status as String) = :status)";

    @Query("select i from Inspection i join fetch i.property p " + OWNED + " and i.uuid = :inspection")
    Optional<Inspection> findOwned(@Param("user") UUID user, @Param("inspection") UUID inspection);

    @Query(value = "select i from Inspection i join fetch i.property p join fetch i.responsible r join fetch r.person " + FILTERED,
            countQuery = "select count(i) from Inspection i join i.property p " + FILTERED)
    Page<Inspection> findAllOwned(@Param("user") UUID user,
                                  @Param("property") UUID property,
                                  @Param("status") String status,
                                  Pageable pageable);

    /** Ambientes e itens ativos de cada vistoria; vistoria sem ambiente não aparece. */
    @Query(value = """
            select a.id_vistoria                                                 as inspection,
                   count(*)                                                      as rooms,
                   count(*) filter (where s.total > 0 and s.total = s.evaluated) as completed,
                   cast(coalesce(sum(s.total), 0) as bigint)                     as items,
                   cast(coalesce(sum(s.evaluated), 0) as bigint)                 as evaluated
            from vistorias.ambiente a
                     cross join lateral (select count(*) as total, count(it.estado) as evaluated
                                         from vistorias.item it
                                         where it.id_ambiente = a.id_ambiente
                                           and it.removido_em is null) s
            where a.id_vistoria in (:inspections)
              and a.removido_em is null
            group by a.id_vistoria""", nativeQuery = true)
    List<InspectionProgress> progress(@Param("inspections") List<Integer> inspections);

    @Procedure(procedureName = "vistorias.copiar_vistoria")
    void copy(@Param("p_id_vistoria") Integer inspection);

    /** A data de finalização vem do relógio do banco, o mesmo que gravou o início. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update vistorias.vistoria set status = 'finalizada', finalizada_em = now() where id_vistoria = :inspection",
            nativeQuery = true)
    int finish(@Param("inspection") Integer inspection);

    @Modifying(flushAutomatically = true)
    @Query(value = "update vistorias.vistoria set removido_em = now() where id_vistoria = :inspection", nativeQuery = true)
    int remove(@Param("inspection") Integer inspection);
}
