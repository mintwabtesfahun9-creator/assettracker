package com.i.a.repository;

import com.i.a.entity.Asset;
import com.i.a.entity.AssetStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AssetRepository
        extends JpaRepository<Asset, Long> {

    List<Asset> findByStatus(AssetStatus status);

    List<Asset> findByAssignedToId(Long userId);

    boolean existsBySerialNumber(String serialNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select asset from Asset asset where asset.id = :id")
    Optional<Asset> findByIdForUpdate(@Param("id") Long id);
}
