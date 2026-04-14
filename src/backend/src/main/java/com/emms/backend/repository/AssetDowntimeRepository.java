package com.emms.backend.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.emms.backend.entity.AssetDowntime;

public interface AssetDowntimeRepository extends JpaRepository<AssetDowntime, Long> {

    List<AssetDowntime> findByAsset_Id(Long assetId);
    @Query("SELECT ad FROM AssetDowntime ad WHERE ad.durationSeconds > 0")
    List<AssetDowntime> findAllWithDuration();

    @Query("SELECT ad FROM AssetDowntime ad WHERE ad.startsOn BETWEEN :start AND :end AND ad.durationSeconds > 0")
    List<AssetDowntime> findByStartsOnBetween(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    // Theo asset + khoảng thời gian
    @Query("SELECT ad FROM AssetDowntime ad WHERE ad.asset.downtimeId = :id AND ad.startsOn BETWEEN :start AND :end AND ad.durationSeconds > 0")
    List<AssetDowntime> findByAsset_IdAndStartsOnBetween(@Param("id") Long id,
                                                        @Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);

    // Lấy tất cả downtime theo asset
    List<AssetDowntime> findByAsset_AssetId(Long assetId);

    // Lọc downtime theo khoảng thời gian
    List<AssetDowntime> findByAsset_AssetIdAndStartsOnBetween1(
            Long assetId,
            Date start,
            Date end
    );
    List<AssetDowntime> findByAsset_AssetIdAndStartsOnBetween(Long assetId, Date start, Date end);
    @Query("""
        select d
        from AssetDowntime d
        where d.startedAt >= :fromDate
          and d.startedAt < :toDate
        order by d.startedAt asc
    """)
    List<AssetDowntime> findByDateRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select d
        from AssetDowntime d
        where d.asset.id = :assetId
          and d.startedAt >= :fromDate
          and d.startedAt < :toDate
        order by d.startedAt asc
    """)
    List<AssetDowntime> findByAssetIdAndDateRange(
            @Param("assetId") Long assetId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
