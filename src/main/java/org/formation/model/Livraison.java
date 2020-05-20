package org.formation.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Livraison {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String noCommande;
	
	@OneToOne
	private Livreur livreur;
	
	private Status status;
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<Trace> historique = new ArrayList<>();

	private Instant creationDate;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNoCommande() {
		return noCommande;
	}

	public void setNoCommande(String noCommande) {
		this.noCommande = noCommande;
	}

	public Livreur getLivreur() {
		return livreur;
	}

	public void setLivreur(Livreur livreur) {
		this.livreur = livreur;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<Trace> getHistorique() {
		return historique;
	}

	public void setHistorique(List<Trace> historique) {
		this.historique = historique;
	}
	
	public void addTrace(Trace trace) {
		historique.add(trace);
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}


}
