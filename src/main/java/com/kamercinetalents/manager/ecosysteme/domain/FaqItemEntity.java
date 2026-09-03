package com.kamercinetalents.manager.ecosysteme.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité JPA représentant une question fréquente (FAQ) du site public.
 *
 * <p>Les questions sont groupées par catégorie et ordonnées via le
 * champ {@code ordre}. Une question inactive ({@code actif=false})
 * n'est pas affichée publiquement.</p>
 */
@Entity
@Table(name = "faq_item")
public class FaqItemEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "reponse", nullable = false)
    private String reponse;

    @Column(name = "categorie", nullable = false)
    private String categorie;

    @Column(name = "ordre", nullable = false)
    private int ordre;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public FaqItemEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
