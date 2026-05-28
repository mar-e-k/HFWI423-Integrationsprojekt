package de.fhdw.vendix.orchestrator.core.domain.register;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface RegisterRepository extends JpaRepository<Register, Long> {

    List<Register> findAllByStoreId(Long storeId);

    @Query(
            """
            SELECT r
            FROM Register r
            WHERE EXISTS (
                SELECT 1
                FROM DistributedLock l
                WHERE l.target.id = r.id
                    AND l.target.type = de.fhdw.vendix.commons.api.embeddable.TargetType.REGISTER
            )
            """
    )
    List<Register> findAllLockedRegisters();

    @Query(
            """
            SELECT r
            FROM Register r
            WHERE NOT EXISTS (
                SELECT 1
                FROM DistributedLock l
                WHERE l.target.id = r.id
                    AND l.target.type = de.fhdw.vendix.commons.api.embeddable.TargetType.REGISTER
            )
            """
    )
    List<Register> findAllNonLockedRegisters();

    @Query(
            """
            SELECT r
            FROM Register r
            WHERE r.storeId = :storeId
                AND EXISTS (
                    SELECT 1
                    FROM DistributedLock l
                    WHERE l.target.id = r.id
                        AND l.target.type = de.fhdw.vendix.commons.api.embeddable.TargetType.REGISTER
            )
            """
    )
    List<Register> findAllLockedRegistersByStoreId(@Param("storeId") Long storeId);

    @Query(
            """
            SELECT r
            FROM Register r
            WHERE r.storeId = :storeId
                AND NOT EXISTS (
                    SELECT 1
                    FROM DistributedLock l
                    WHERE l.target.id = r.id
                        AND l.target.type = de.fhdw.vendix.commons.api.embeddable.TargetType.REGISTER
            )
            """
    )
    List<Register> findAllNonLockedRegistersByStoreId(@Param("storeId") Long storeId);
}
