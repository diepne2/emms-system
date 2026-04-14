package com.emms.backend.mapper;

import com.emms.backend.dto.file.FileShowDTO;
import com.emms.backend.dto.file.FileSummaryDTO;
import com.emms.backend.entity.File;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {

    @Mapping(source = "path", target = "url")
    FileShowDTO toShowDto(File model);

    @Mapping(source = "path", target = "url")
    FileSummaryDTO toSummaryDto(File model);
}