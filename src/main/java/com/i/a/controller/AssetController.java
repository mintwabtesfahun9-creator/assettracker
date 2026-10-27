package com.i.a.controller;

import com.i.a.dto.AssetRequest;
import com.i.a.dto.AssetResponse;
import com.i.a.entity.AssetStatus;
import com.i.a.service.AssetService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<AssetResponse> getAllAssets(
            @RequestParam(required = false) AssetStatus status) {

        return assetService.getAllAssets(status);
    }

    @GetMapping("/my-assets")
    public List<AssetResponse> getMyAssets(
            Authentication authentication) {

        return assetService.getMyAssets(
                authentication.getName()
        );
    }

    @PostMapping
    public ResponseEntity<AssetResponse> createAsset(
            @Valid @RequestBody AssetRequest request) {

        AssetResponse response =
                assetService.createAsset(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/{id}/assign/{userId}")
    public AssetResponse assignAsset(
            @PathVariable Long id,
            @PathVariable Long userId) {

        return assetService.assignAsset(id, userId);
    }

    @PatchMapping("/{id}/return")
    public AssetResponse returnAsset(
            @PathVariable Long id) {

        return assetService.returnAsset(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(
            @PathVariable Long id) {

        assetService.deleteAsset(id);

        return ResponseEntity.noContent().build();
    }
}
