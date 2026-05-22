package com.emms.backend.repository;

import com.emms.backend.entity.AssetDowntime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AssetDowntimeRepository extends JpaRepository<AssetDowntime, Long> {

    List<AssetDowntime> findByAsset_Id(Long assetId);

    Long countByAsset_Id(Long assetId);

    @Query("""
        SELECT ad
        FROM AssetDowntime ad
        WHERE ad.startsOn BETWEEN :start AND :end
    """)
    List<AssetDowntime> findByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT ad
        FROM AssetDowntime ad
        WHERE ad.asset.id = :assetId
          AND ad.startsOn BETWEEN :start AND :end
    """)
    List<AssetDowntime> findByAssetIdAndDateRange(
            @Param("assetId") Long assetId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT d.asset.id, COUNT(d)
        FROM AssetDowntime d
        WHERE MONTH(d.createdAt) = :month
          AND YEAR(d.createdAt) = :year
        GROUP BY d.asset.id
    """)
    List<Object[]> countMonthlyByAsset(
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
        SELECT d.asset.id, COUNT(d)
        FROM AssetDowntime d
        WHERE d.createdAt >= :fromDate
        GROUP BY d.asset.id
    """)
    List<Object[]> countRecentByAsset(
            @Param("fromDate") LocalDateTime fromDate
    );

@Query("""
    select d.asset.id,
           sum(function('timestampdiff', hour, d.startsOn, d.endsOn))
    from AssetDowntime d
    where d.startsOn >= :fromDate
      and d.startsOn < :toDate
    group by d.asset.id
""")
List<Object[]> sumDowntimeHoursByAsset(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
);

@Query("""
SELECT d.asset.id, COUNT(d)
FROM AssetDowntime d
WHERE d.startsOn >= :from
AND d.startsOn < :to
GROUP BY d.asset.id
""")
List<Object[]> countByAssetBetweenDates(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
);

    @Query("""
        SELECT d.asset.id, COALESCE(SUM(d.durationSeconds), 0)
        FROM AssetDowntime d
        WHERE d.startsOn >= :from
          AND d.startsOn < :to
        GROUP BY d.asset.id
    """)
    List<Object[]> countByAssetBetweenDates1(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

}