package org.formation.resource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.formation.model.Livraison;
import org.formation.model.Status;
import org.formation.model.Trace;
import org.formation.repository.LivraisonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/livraison")
public class LivraisonResource {

	protected Logger logger = Logger.getLogger(LivraisonResource.class.getName());

	@Autowired
	LivraisonRepository livraisonRepository;

	@GetMapping
	public List<Livraison> findAll() {
		return livraisonRepository.findAll();
	}

	@GetMapping("/{livraisonId}")
	public ResponseEntity<Livraison> byId(@PathVariable("livraisonId") long livraisonId) {

		logger.info("Livraison-resource byId() invoked: " + livraisonId);
		Optional<Livraison> optLivraison = livraisonRepository.findById(livraisonId);
		if (!optLivraison.isPresent()) {
			throw new LivraisonNotFoundException("" + livraisonId);
		}
		logger.info("Members-service byNumber() found: " + optLivraison.get());

		return ResponseEntity.ok().header("Content-type", "application/json").body(optLivraison.get());
	}

	@PostMapping
	public ResponseEntity<Livraison> createLivraison(String noCommande) {

		Livraison livraison = new Livraison();
		livraison.setNoCommande(noCommande);
		Trace trace = new Trace();
		trace.setNewStatus(Status.CREE);
		trace.setDate(Instant.now());
		livraison.addTrace(trace);
		livraison.setCreationDate(trace.getDate());
		livraison.setStatus(Status.CREE);
		livraison = livraisonRepository.save(livraison);

		return new ResponseEntity<>(livraison, HttpStatus.CREATED);

	}
	
	@PostMapping("/{livraisonId}")
	public ResponseEntity<Livraison> changeStatus(@PathVariable("livraisonId") long livraisonId, @RequestBody Status newStatus) {

		Optional<Livraison> optLivraison = livraisonRepository.findById(livraisonId);
		if (!optLivraison.isPresent()) {
			throw new LivraisonNotFoundException("" + livraisonId);
		}
		Livraison livraison = optLivraison.get();
		Status oldStatus = livraison.getStatus();
		Trace trace = new Trace();
		trace.setOldStatus(oldStatus);
		trace.setNewStatus(newStatus);
		trace.setDate(Instant.now());
		livraison.addTrace(trace);
		livraison.setStatus(newStatus);
		livraison = livraisonRepository.save(livraison);

		return new ResponseEntity<>(livraison, HttpStatus.OK);

	}
}
