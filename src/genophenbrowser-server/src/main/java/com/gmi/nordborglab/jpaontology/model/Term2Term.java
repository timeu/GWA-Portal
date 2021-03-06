package com.gmi.nordborglab.jpaontology.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="term2term")
@SequenceGenerator(name="idSequence",sequenceName="term2term_id_seq")
public class Term2Term extends BaseOntologyEntity {
	
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "relationship_type_id")
	private Term relationshipType;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "term1_id")
	private Term parent;
	
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "term2_id")
	private Term child;

	private Boolean complete;

	public Term getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(Term relationshipType) {
		this.relationshipType = relationshipType;
	}

	public Term getParent() {
		return parent;
	}

	public void setParent(Term parent) {
		this.parent = parent;
	}

	public Term getChild() {
		return child;
	}

	public void setChild(Term child) {
		this.child = child;
	}

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}
}
