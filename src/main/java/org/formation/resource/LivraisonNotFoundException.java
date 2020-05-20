package org.formation.resource;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LivraisonNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LivraisonNotFoundException(String criteria) {
		super("Pas de livraison trouvée pour : " + criteria);
	}

}
