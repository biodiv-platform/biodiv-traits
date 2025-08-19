/**
 *
 */
package com.strandls.traits.pojo;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Abhishek Rudra
 *
 */
@Schema
public class FactValuePair {

	private Long nameId;
	private String name;
	private Long valueId;
//	it will hold , trait value, fromValue-toValue format v1:v2
	private String value;
	private Date fromDate;
	private Date toDate;
	private String type;
	private Boolean isParticipatry;

	/**
	 *
	 */
	public FactValuePair() {
		super();
	}

	/**
	 * @param nameId
	 * @param name
	 * @param valueId
	 * @param value
	 * @param fromDate
	 * @param toDate
	 * @param type
	 * @param isParticipatry
	 */
	public FactValuePair(Long nameId, String name, Long valueId, String value, Date fromDate, Date toDate, String type,
			Boolean isParticipatry) {
		super();
		this.nameId = nameId;
		this.name = name;
		this.valueId = valueId;
		this.value = value;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.type = type;
		this.isParticipatry = isParticipatry;
	}

	public Long getNameId() {
		return nameId;
	}

	public void setNameId(Long nameId) {
		this.nameId = nameId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getValueId() {
		return valueId;
	}

	public void setValueId(Long valueId) {
		this.valueId = valueId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIsParticipatry() {
		return isParticipatry;
	}

	public void setIsParticipatry(Boolean isParticipatry) {
		this.isParticipatry = isParticipatry;
	}

}
