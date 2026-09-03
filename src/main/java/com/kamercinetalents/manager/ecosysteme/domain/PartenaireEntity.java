package com.kamercinetalents.manager.ecosysteme.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant un partenaire (logo + lien) du site public.
 */
@Entity
@Table(name = "partenaire")
public class PartenaireEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "site_web")
    private String siteWeb;

    @Column(name = "ordre", nullable = false)
    private int ordre;

    public PartenaireEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
}
