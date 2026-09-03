package com.kamercinetalents.manager.ecosysteme.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une actualité publique du site vitrine.
 *
 * <p>Les actualités ont deux statuts : {@code brouillon} (non visible
 * publiquement) et {@code publiee} (visible sur le site). La suppression
 * est douce ({@code deletedAt}) pour préserver l'historique.</p>
 */
@Entity
@Table(name = "actualite_publique")
public class ActualitePubliqueEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "contenu", nullable = false)
    private String contenu;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "date_publication", nullable = false)
    private OffsetDateTime datePublication;

    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public ActualitePubliqueEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public OffsetDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(OffsetDateTime datePublication) { this.datePublication = datePublication; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
