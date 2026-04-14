package com.emms.backend.repository;

import com.emms.backend.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long>, JpaSpecificationExecutor<Vendor> {

    Optional<Vendor> findByCompanyNameIgnoreCase(String companyName);

    Optional<Vendor> findByVendorCodeIgnoreCase(String vendorCode);
}