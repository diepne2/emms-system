package com.emms.backend.repository;

import com.emms.backend.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByComment_Id(Long commentId);

}