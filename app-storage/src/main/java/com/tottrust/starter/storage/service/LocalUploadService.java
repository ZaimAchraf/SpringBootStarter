package com.tottrust.starter.storage.service;

import com.tottrust.starter.storage.interfaces.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
@Slf4j
public class LocalUploadService implements UploadService {

    private final String uploadsRoot; // ex: "./api/uploads" ou "C:/.../api/uploads"

    public LocalUploadService(@Value("${uploads.dir:./api/uploads}") String uploadsDirProperty) throws IOException {
        this.uploadsRoot = new File(uploadsDirProperty).getAbsolutePath();
        // ensure root exists
        Path root = Paths.get(uploadsRoot);
        if (!Files.exists(root)) {
            log.info("Création du dossier racine upload {}", root.toAbsolutePath());
            Files.createDirectories(root);
        } else {
            log.info("Upload root existe: {}", root.toAbsolutePath());
        }
    }

    /**
     * namespace: sous-dossier (ex: "accessories", "product-docs")
     * logicalNameHint: suggestion (ex: "product-12" or original filename) pour construire le safe name
     */
    @Override
    public String store(String namespace, String logicalNameHint, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Fichier manquant");
        }

        Path nsDir = Paths.get(uploadsRoot, namespace);
        if (!Files.exists(nsDir)) {
            Files.createDirectories(nsDir);
        }

        String originalName = Objects.requireNonNull(file.getOriginalFilename(), "original filename null");

        // extension du fichier uploadé
        String originalExt = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            originalExt = originalName.substring(dot);
        }

        // base du nom
        String base;
        if (logicalNameHint != null && !logicalNameHint.isBlank()) {
            base = logicalNameHint.replaceAll("[^a-zA-Z0-9\\-_.]", "_");
        } else {
            base = String.valueOf(System.currentTimeMillis());
        }

        // ➜ éviter la double extension
        String safeName;
        if (base.contains(".")) {
            safeName = base; // extension déjà présente
        } else {
            safeName = base + originalExt;
        }

        Path target = nsDir.resolve(safeName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.info("Stocké {} -> {}", originalName, target.toAbsolutePath());

        // chemin relatif
        return safeName;
    }


    @Override
    public void delete(String storedName) {
        if (storedName == null) return;
        try {
            Path p = Paths.get(uploadsRoot, storedName);
            File f = p.toFile();
            if (f.exists()) {
                if (!f.delete()) {
                    log.warn("Impossible de supprimer {}", p.toAbsolutePath());
                } else {
                    log.info("Supprimé {}", p.toAbsolutePath());
                }
            } else {
                log.debug("Fichier à supprimer introuvable: {}", p.toAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Erreur suppression {} : {}", storedName, e.getMessage());
        }
    }

    @Override
    public String absolutePathFor(String storedName) {
        if (storedName == null) return null;
        return Paths.get(uploadsRoot, storedName).toAbsolutePath().toString();
    }
}
