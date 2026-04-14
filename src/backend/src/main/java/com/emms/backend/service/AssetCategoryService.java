package com.emms.backend.service;

import com.emms.backend.dto.asset.AssetPUTDTO;
import com.emms.backend.entity.AssetCategory;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.AssetCategoryMapper;
import com.emms.backend.repository.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.lang.StackWalker.Option;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetCategoryService {

    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetCategoryMapper assetCategoryMapper;

    public AssetCategory create (AssetCategory assetCategory) {
        Optional<AssetCategory> assetWithSameName = assetCategoryRepository.findByNameIgnoreCase(assetCategory.getName());
        
        if (assetWithSameName.isPresent()) {
            throw new CustomException("Loại thiết bị cùng tên đã tồn tại.", HttpStatus.NOT_ACCEPTABLE);
        }
        return assetCategoryRepository.save(assetCategory);
    }

    public AssetCategory update (Long id, AssetCategory assetCategory) {
        if (assetCategoryRepository.existsById(id)) {
            AssetCategory saveAssetCategory = assetCategoryRepository.findById(id).get();
            return assetCategoryRepository.save(assetCategoryMapper.updateAssetCategory(saveAssetCategory,assetCategory));
        } else throw new CustomException("Not Found" ,HttpStatus.NOT_FOUND );
    }

    public Collection<AssetCategory> getAll() {
        return assetCategoryRepository.findAll();
    }

    public void delete(Long id) {
        assetCategoryRepository.deleteById(id);
    }

    public AssetCategory getById(Long id) {
        return assetCategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy category"));
    }

    public Optional<AssetCategory> findByNameIgnoreCase(String name) {
        return assetCategoryRepository.findByNameIgnoreCase(name);
    }

}