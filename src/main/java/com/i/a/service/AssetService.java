package com.i.a.service;

import com.i.a.dto.AssetRequest;
import com.i.a.dto.AssetResponse;
import com.i.a.entity.*;
import com.i.a.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AssetService(
            AssetRepository assetRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets(AssetStatus status) {
        List<Asset> assets = status == null
            ? assetRepository.findAll()
            : assetRepository.findByStatus(status);

        return assets.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getMyAssets(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));

        return assetRepository.findByAssignedToId(user.getId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AssetResponse createAsset(AssetRequest request) {

        if (assetRepository.existsBySerialNumber(
                request.serialNumber().trim())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Serial number already exists");
        }

        Category category = categoryRepository
            .findById(request.categoryId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Category not found"));

        Asset asset = new Asset();
        asset.setName(request.name().trim());
        asset.setSerialNumber(request.serialNumber().trim());
        asset.setCategory(category);
        asset.setStatus(AssetStatus.AVAILABLE);

        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse assignAsset(Long assetId, Long userId) {
        Asset asset = assetRepository.findByIdForUpdate(assetId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Asset not found"));

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only AVAILABLE assets can be assigned");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));

        asset.setAssignedTo(user);
        asset.setStatus(AssetStatus.ASSIGNED);

        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse returnAsset(Long assetId) {
        Asset asset = findAsset(assetId);

        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only ASSIGNED assets can be returned");
        }

        asset.setAssignedTo(null);
        asset.setStatus(AssetStatus.AVAILABLE);

        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public void deleteAsset(Long assetId) {
        Asset asset = findAsset(assetId);
        assetRepository.delete(asset);
    }

    private Asset findAsset(Long id) {
        return assetRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Asset not found"));
    }

    private AssetResponse toResponse(Asset asset) {
        User assigned = asset.getAssignedTo();

        return new AssetResponse(
            asset.getId(),
            asset.getName(),
            asset.getSerialNumber(),
            asset.getStatus(),
            asset.getCategory().getId(),
            asset.getCategory().getName(),
            assigned == null ? null : assigned.getId(),
            assigned == null ? null : assigned.getUsername(),
            asset.getCreatedAt()
        );
    }
}
