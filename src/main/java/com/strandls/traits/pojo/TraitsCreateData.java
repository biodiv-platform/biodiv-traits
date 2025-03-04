/**
 * 
 */
package com.strandls.traits.pojo;

import java.util.List;

/**
 * @author Mekala Rishitha Ravi
 *
 */
public class TraitsCreateData {

	private Traits traits;
	private List<TraitsValue> values;
	private List<TraitTaxonomyDefinition> query;

	/**
	 * @param traits
	 * @param values
	 */
	public TraitsCreateData() {
		super();
	}

	public Traits getTraits() {
		return traits;
	}

	public void setTraits(Traits traits) {
		this.traits = traits;
	}

	public List<TraitsValue> getValues() {
		return values;
	}

	public void setValues(List<TraitsValue> values) {
		this.values = values;
	}

	public List<TraitTaxonomyDefinition> getQuery() {
		return query;
	}

	public void setQuery(List<TraitTaxonomyDefinition> query) {
		this.query = query;
	}

}
