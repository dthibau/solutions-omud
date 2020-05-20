package org.formation.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Livreur {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	

}
