package com.greenshare.entity.offer;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.greenshare.entity.abstracts.AbstractEntity;
import com.greenshare.entity.user.User;

/**
 * Persistence class for the table offer_comment
 * 
 * @author joao.silva
 */
@Entity
@Table(name = "offer_comment")
public class OfferComment extends AbstractEntity<OfferComment> implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String SEQUENCE_NAME = "offer_comment_seq";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = SEQUENCE_NAME)
	@SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME)
	@Basic(optional = false)
	@Column(name = "offer_comment_id")
	private Long id;

	@Basic(optional = false)
	@NotNull(message = "O texto não pode ser nulo.")
	@Size(min = 1, max = 2500, message = "O texto deve conter de 1 a 250 caracteres")
	@Column(name = "text", length = 250)
	private String text;

	@ManyToOne
	@Basic(optional = false)
	@NotNull(message = "O usuário não pode ser nulo.")
	@Valid
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JsonIgnore
	@Basic(optional = false)
	@NotNull(message = "A oferta não pode ser nula.")
	@Valid
	@JoinColumn(name = "offer_id")
	private Offer offer;

	protected OfferComment() {
		super(false);
	}

	public OfferComment(String text, User user, Offer offer) {
		super(true);
		this.text = text;
		this.user = user;
		this.offer = offer;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public boolean isValid() {
		this.validationErrors.clear();

		if (isNullOrEmpty(this.text) || is(this.text).orSmallerThan(1).orBiggerThan(250)) {
			this.validationErrors.add("O texto deve conter de 1 a 250 caracteres");
		}
		if (isNull(this.user) || !this.user.getIsLegalPerson()) {
			this.validationErrors.add("Usuário inválido.");
		} else if (this.user.isNotValid()) {
			this.validationErrors.addAll(this.user.getValidationErrors());
		}
		if (isNull(this.offer)) {
			this.validationErrors.add("Oferta não pode ser nula.");
		} else if (this.user.isNotValid()) {
			this.validationErrors.addAll(this.user.getValidationErrors());
		}

		return this.validationErrors.isEmpty();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public User getUser() {
		return user;
	}

	public Offer getOffer() {
		return offer;
	}

	@Override
	public void update(OfferComment e) {
		this.text = e.getText();	
	}

}
