/**
 * 
 */
package com.strandls.traits.pojo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Abhishek Rudra
 *
 */
@Entity
@Table(name = "trait")
public class Traits implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7750012729432714454L;
	private Long id;
	private Date createdOn;
	private Date lastRevised;
	private String dataType;
	private String description;
	private Long fieldId;
	private String name;
	private String traitTypes;
	private String units;
	private Boolean isNotObservationTraits;
	private Boolean showInObservation;
	private Boolean isParticipatory;
	private Boolean isDeleted;
	private String source;
	private String icon;
	private Long traitId;
	private Long languageId;

	@Id
	@GeneratedValue
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "created_on")
	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	@Column(name = "last_revised")
	public Date getLastRevised() {
		return lastRevised;
	}

	public void setLastRevised(Date lastRevised) {
		this.lastRevised = lastRevised;
	}

	@Column(name = "data_types")
	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@Column(name = "description", columnDefinition = "TEXT")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "field_id")
	public Long getFieldId() {
		return fieldId;
	}

	public void setFieldId(Long fieldId) {
		this.fieldId = fieldId;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "trait_types")
	public String getTraitTypes() {
		return traitTypes;
	}

	public void setTraitTypes(String traitTypes) {
		this.traitTypes = traitTypes;
	}

	@Column(name = "show_in_observation")
	public Boolean getShowInObservation() {
		return showInObservation;
	}

	public void setShowInObservation(Boolean showInObservation) {
		this.showInObservation = showInObservation;
	}

	@Column(name = "is_participatory")
	public Boolean getIsParticipatory() {
		return isParticipatory;
	}

	public void setIsParticipatory(Boolean isParticipatory) {
		this.isParticipatory = isParticipatory;
	}

	@Column(name = "is_deleted")
	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Column(name = "source")
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Column(name = "icon")
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Column(name = "units")
	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	@Column(name = "is_not_observation_trait")
	public Boolean getIsNotObservationTraits() {
		return isNotObservationTraits;
	}

	public void setIsNotObservationTraits(Boolean isNotObservationTraits) {
		this.isNotObservationTraits = isNotObservationTraits;
	}
	
	@Column(name = "trait_id")
	public Long getTraitId() {
		return traitId;
	}

	public void setTraitId(Long traitId) {
		this.traitId = traitId;
	}
	
	@Column(name = "language_id")
	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

}
