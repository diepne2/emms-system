package com.emms.backend.repository;

import com.emms.backend.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long>, JpaSpecificationExecutor<Part> {

    Optional<Part> findById(Long id);

    List<Part> findByIdIn(List<Long> ids);

    List<Part> findAllByOrderByNameAsc();

    Optional<Part> findByNameIgnoreCase(String name);

    Optional<Part> findByBarcode(String barcode);

    Collection<Part> findByCategoryIgnoreCase(String category);

    Collection<Part> findByConsumable(Boolean consumable);

    Collection<Part> findByVendorIgnoreCase(String vendor);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    @Query("SELECT CASE WHEN COUNT(p) > ?1 THEN true ELSE false END FROM Part p")
    boolean hasMoreThan(Long threshold);
}