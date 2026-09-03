package com.kamercinetalents.manager.pilotage.service;

import com.kamercinetalents.manager.formation.repository.AttestationRepository;
import com.kamercinetalents.manager.formation.repository.ResultatExamenRepository;
import com.kamercinetalents.manager.pilotage.dto.CarteDto;
import com.kamercinetalents.manager.pilotage.dto.IndicateurDto;
import com.kamercinetalents.manager.territoire.dto.CommuneDto;
import com.kamercinetalents.manager.territoire.service.TerritoireService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service du module Pilotage (M4) — consolide les indicateurs clés
 * et les données de la carte interactive.
 *
 * <p>Les indicateurs sont calculés à partir des données du périmètre
 * de l'utilisateur connecté. Le contrôle territorial est délégué au
 * {@link TerritoireService} qui filtre les communes accessibles.</p>
 */
@Service
@Transactional(readOnly = true)
public class PilotageService {

    private final TerritoireService territoireService;
    private final AttestationRepository attestationRepository;
    private final ResultatExamenRepository resultatExamenRepository;

    public PilotageService(
            TerritoireService territoireService,
            AttestationRepository attestationRepository,
            ResultatExamenRepository resultatExamenRepository) {
        this.territoireService = territoireService;
        this.attestationRepository = attestationRepository;
        this.resultatExamenRepository = resultatExamenRepository;
    }

    /**
     * Calcule les indicateurs clés du périmètre de l'utilisateur.
     *
     * @return la liste des indicateurs
     */
    public List<IndicateurDto> getIndicateurs() {
        List<CommuneDto> communes = territoireService.getCommunes();

        long communesActives = communes.stream()
                .filter(c -> "active".equals(c.statutCommune()) || "terminee".equals(c.statutCommune()))
                .count();
        long totalApprenants = communes.stream().mapToLong(CommuneDto::nombreApprenants).sum();
        long totalEncadreurs = communes.stream().mapToLong(CommuneDto::nombreEncadreurs).sum();
        long totalSessions = communes.stream().mapToLong(CommuneDto::nombreSessions).sum();
        long totalAttestations = attestationRepository.count();
        long totalResultats = resultatExamenRepository.count();
        long reussis = resultatExamenRepository.findAll().stream()
                .filter(r -> r.getNote() != null && r.getNote().doubleValue() >= 50.0)
                .count();
        long tauxReussite = totalResultats > 0 ? (reussis * 100 / totalResultats) : 0;

        return List.of(
                new IndicateurDto("Communes actives", communesActives, ""),
                new IndicateurDto("Apprenants", totalApprenants, ""),
                new IndicateurDto("Encadreurs", totalEncadreurs, ""),
                new IndicateurDto("Sessions", totalSessions, ""),
                new IndicateurDto("Taux de réussite", tauxReussite, "%"),
                new IndicateurDto("Attestations émises", totalAttestations, "")
        );
    }

    /**
     * Récupère les données de la carte (communes + statut + compteurs).
     *
     * @return les données de la carte
     */
    public CarteDto getCarteData() {
        List<CommuneDto> communes = territoireService.getCommunes();
        return new CarteDto(communes);
    }
}
