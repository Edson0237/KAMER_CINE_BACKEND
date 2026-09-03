package com.kamercinetalents.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'API KAMER CINÉ TALENTS MANAGER.
 *
 * <p>Cette application Spring Boot expose une API REST modulaire (M0-M13)
 * pour piloter le programme de formation aux métiers du cinéma sur les
 * 360 communes du Cameroun. Chaque module métier est encapsulé dans son
 * propre sous-package, sans couplage transverse entre modules.</p>
 */
@SpringBootApplication
public class KctManagerApiApplication {

	/**
	 * Démarre l'application Spring Boot.
	 *
	 * @param args arguments de ligne de commande transmis à Spring
	 */
	public static void main(String[] args) {
		SpringApplication.run(KctManagerApiApplication.class, args);
	}

}
