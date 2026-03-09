package com.tottrust.starter.storage.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Contrat générique pour stocker / supprimer des fichiers.
 * L'implémentation décide du dossier, du nom final, etc.
 */
public interface UploadService {
    /**
     * Stocke le fichier dans le "namespace" donné (ex: "accessories", "product-docs").
     * Retourne le nom du fichier enregistré (ex: "accessories/123_image.png" ou juste "123_image.png" suivant impl).
     */
    String store(String namespace, String logicalNameHint, MultipartFile file) throws IOException;

    /**
     * Supprime un fichier déjà enregistré (nom tel que retourné par store).
     */
    void delete(String storedName);

    /**
     * Retourne le chemin absolu local pour un fichier stocké (null si non applicable).
     */
    String absolutePathFor(String storedName);
}

