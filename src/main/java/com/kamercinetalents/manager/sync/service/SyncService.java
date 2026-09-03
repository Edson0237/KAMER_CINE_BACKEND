package com.kamercinetalents.manager.sync.service;

import com.kamercinetalents.manager.sync.dto.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Service de synchronisation hors-ligne — module M5.
 *
 * <p>Implémente le flux décrit dans {@code Sequence_Synchronisation_M5.puml} :
 * <ol>
 *   <li>Réception d'un lot d'actions via POST /api/sync</li>
 *   <li>Vérification JWT + périmètre territorial (via intercepteur Spring Security)</li>
 *   <li>Pour chaque action (transaction indépendante via {@link SyncActionProcessor}) :
 *     <ul>
 *       <li>Si entité absente côté serveur → INSERT, statut = 'applied'</li>
 *       <li>Si entité présente et server_updated_at ≤ client_updated_at → UPDATE, statut = 'applied'</li>
 *       <li>Si conflit (modification concurrente) → Last Write Wins sur horodatage_client,
 *           journalisation dans sync_conflict_log, statut = 'conflict'</li>
 *       <li>Si retry (action.id déjà traité) → renvoi direct du statut précédent (idempotence)</li>
 *     </ul>
 *   </li>
 *   <li>Retour de l'accusé de réception avec le statut de chaque action</li>
 * </ol>
 *
 * <p><b>Garantie ACID :</b> chaque action est traitée dans sa propre transaction
 * ({@code Propagation.REQUIRES_NEW}) via {@link SyncActionProcessor}, un bean
 * séparé pour garantir que le proxy Spring honore l'annotation. Un échec sur
 * une action n'annule pas les autres — aucune écriture partielle visible.</p>
 */
@org.springframework.stereotype.Service
public class SyncService {

    private final SyncActionProcessor actionProcessor;

    public SyncService(SyncActionProcessor actionProcessor) {
        this.actionProcessor = actionProcessor;
    }

    /**
     * Traite un lot d'actions de synchronisation.
     *
     * <p>Chaque action est traitée dans sa propre transaction (Propagation.REQUIRES_NEW)
     * via {@link SyncActionProcessor} afin qu'un échec sur une action n'annule pas les
     * autres. Aucune écriture partielle n'est visible en cas d'erreur.</p>
     *
     * @param request le lot d'actions avec utilisateur_id et device_id
     * @return l'accusé de réception avec le statut de chaque action
     */
    public SyncResponseDto synchronize(SyncRequestDto request) {
        List<SyncResultDto> resultats = new ArrayList<>();
        int applied = 0, conflicts = 0, rejected = 0;

        for (SyncActionDto action : request.actions()) {
            SyncResultDto result = actionProcessor.processAction(action, request.utilisateurId());
            resultats.add(result);

            switch (result.statut()) {
                case "applied" -> applied++;
                case "conflict" -> conflicts++;
                case "rejected" -> rejected++;
            }
        }

        return new SyncResponseDto(request.actions().size(), applied, conflicts, rejected, resultats);
    }
}
