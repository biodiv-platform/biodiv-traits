/**
 * 
 */
package com.strandls.traits.pojo;

import java.io.Serializable;

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
@Table(name = "trait_value")
public class TraitsValue implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2680415512611318294L;

	private Long id;
	private String value;
	private String icon;
	private Long traitInstanceId;
	private String description;
	private String source;
	private Boolean isDeleted;
	private Long displayOrder;
	private Long traitValueId;
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

	@Column(name = "value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Column(name = "display_order", columnDefinition = "BIGINT")
	public Long getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Long displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	@Column(name = "trait_instance_id")
	public Long getTraitInstanceId() {
		return traitInstanceId;
	}

	public void setTraitInstanceId(Long traitInstanceId) {
		this.traitInstanceId = traitInstanceId;
	}

	@Column(name = "is_deleted")
	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Column(name = "trait_value_id")
	public Long getTraitValueId() {
		return traitValueId;
	}

	public void setTraitValueId(Long traitValueId) {
		this.traitValueId = traitValueId;
	}

	@Column(name = "language_id")
	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

}
