package com.emms.backend.repository;

import com.emms.backend.entity.AssetDowntime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AssetDowntimeRepository extends JpaRepository<AssetDowntime, Long> {

    List<AssetDowntime> findByAsset_Id(Long assetId);

    @Query("SELECT ad FROM AssetDowntime ad WHERE ad.durationSeconds > 0")
    List<AssetDowntime> findAllWithDuration();

    @Query("""
           SELECT ad
           FROM AssetDowntime ad
           WHERE ad.startsOn BETWEEN :start AND :end
             AND ad.durationSeconds > 0
           """)
    List<AssetDowntime> findByStartsOnBetween(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("""
           SELECT ad
           FROM AssetDowntime ad
           WHERE ad.asset.id = :id
             AND ad.startsOn BETWEEN :start AND :end
             AND ad.durationSeconds > 0
           """)
    List<AssetDowntime> findByAsset_IdAndStartsOnBetween(@Param("id") Long id,
                                                         @Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);

    @Query("""
           SELECT d
           FROM AssetDowntime d
           WHERE d.startsOn >= :fromDate
             AND d.startsOn < :toDate
           ORDER BY d.startsOn ASC
           """)
    List<AssetDowntime> findByDateRange(@Param("fromDate") LocalDateTime fromDate,
                                        @Param("toDate") LocalDateTime toDate);

    @Query("""
           SELECT d
           FROM AssetDowntime d
           WHERE d.asset.id = :assetId
             AND d.startsOn >= :fromDate
             AND d.startsOn < :toDate
           ORDER BY d.startsOn ASC
           """)
    List<AssetDowntime> findByAssetIdAndDateRange(@Param("assetId") Long assetId,
                                                  @Param("fromDate") LocalDateTime fromDate,
                                                  @Param("toDate") LocalDateTime toDate);
}