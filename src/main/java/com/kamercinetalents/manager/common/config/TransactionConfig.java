package com.kamercinetalents.manager.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuration transactionnelle globale de l'application.
 *
 * <p>Niveau d'isolation par défaut : {@code READ_COMMITTED}, conformément
 * aux exigences ACID du projet. Chaque opération multi-tables doit être
 * annotée {@code @Transactional} avec des limites explicites (au niveau
 * service, jamais au niveau contrôleur).</p>
 *
 * <p><strong>Règle de gestion transactionnelle (non négociable) :</strong>
 * <ul>
 *   <li>Les transactions sont déclarées au niveau des méthodes de service.</li>
 *   <li>Aucune transaction ne doit englober un appel réseau ou un traitement long.</li>
 *   <li>Les opérations de lecture utilisent {@code @Transactional(readOnly = true)}.</li>
 *   <li>Les opérations d'écriture multi-tables utilisent {@code @Transactional} explicite.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * Configure le gestionnaire de transactions JPA avec un timeout
     * par défaut de 30 secondes pour éviter les transactions longues.
     *
     * @param entityManagerFactory la factory JPA injectée par Spring
     * @return le gestionnaire de transactions configuré
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        txManager.setDefaultTimeout(30);
        return txManager;
    }
}
