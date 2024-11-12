/**
 * 
 */
package com.strandls.traits.services;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.strandls.traits.pojo.FactValuePair;
import com.strandls.traits.pojo.Facts;
import com.strandls.traits.pojo.FactsCreateData;
import com.strandls.traits.pojo.FactsUpdateData;
import com.strandls.traits.pojo.Traits;
import com.strandls.traits.pojo.TraitsValue;
import com.strandls.traits.pojo.TraitsValuePair;

/**
 * @author Abhishek Rudra
 *
 */
public interface TraitsServices {

	public List<FactValuePair> getFacts(String objectType, Long objectId, Long traitId);

	public FactValuePair getFactIbp(Long id);

	public List<TraitsValuePair> getAllObservationTraits();

	public List<TraitsValuePair> getObservationTraitList(Long speciesId);
	
	public String createTraits (String dataType, String description, Long fieldId, String name, String traitTypes,
			String units, Boolean showInObservation, Boolean isParticipatory, String values);

	public List<FactValuePair> createFacts(HttpServletRequest request, String objectType, Long objectId,
			FactsCreateData factsCreateData);
	
	public Map<String, Object> fetchByTraitId(Long traitId);

	public List<Facts> fetchByTaxonId(Long taxonId);

	public List<Long> fetchTaxonIdByValueId(String valueList);

	public List<TraitsValue> fetchTraitsValue(Long traitId);

	public List<FactValuePair> updateTraits(HttpServletRequest request, String objectType, Long objectId, Long traitId,
			FactsUpdateData factsUpdateData);

	public List<TraitsValuePair> getSpeciesTraits(Long taxonId);

	public List<TraitsValuePair> getAllSpeciesTraits();

}
