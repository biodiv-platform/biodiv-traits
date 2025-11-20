/**
 * 
 */
package com.strandls.traits.services;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.strandls.traits.pojo.FactValuePair;
import com.strandls.traits.pojo.Facts;
import com.strandls.traits.pojo.FactsCreateData;
import com.strandls.traits.pojo.FactsUpdateData;
import com.strandls.traits.pojo.Traits;
import com.strandls.traits.pojo.TraitsCreateData;
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

	public List<TraitsValuePair> getObservationTraitList(Long speciesId, Long language);

	public String createTraits(List<TraitsCreateData> traitsCreateData);

	public String updateTraits(Long id, List<TraitsCreateData> traitsUpdateData);

	public List<FactValuePair> createFacts(HttpServletRequest request, String objectType, Long objectId,
			FactsCreateData factsCreateData);

	public Map<String, Object> fetchByTraitIdByLanguageId(Long traitId, Long languageId);

	public List<Facts> fetchByTaxonId(Long taxonId);

	public List<Long> fetchTaxonIdByValueId(String valueList);

	public List<TraitsValue> fetchTraitsValue(Long traitId);

	public String bulkTraitsUpdate(HttpServletRequest request, String objectType, Long objectId,
			Map<String, List> factsAddData, String userId, String taxonId);

	public List<FactValuePair> updateTraits(HttpServletRequest request, String objectType, Long objectId, Long traitId,
			FactsUpdateData factsUpdateData);

	public List<TraitsValuePair> getAllTraits(Long language);

	public List<TraitsValuePair> getSpeciesTraits(Long taxonId, Long language);

	public List<TraitsValuePair> getAllSpeciesTraits(Long language);

	public List<Map<String, String>> importSpeciesTraits(FormDataBodyPart file, List<String> traits,
			String scientificNameColumn, String taxonColumn, String speciesIdColumn, String contributorColumn,
			String attributionColumn, String licenseColumn);

	public List<Map<String, Object>> fetchByTraitId(Long traitId);

	public List<Traits> getAllTraitsNames();

	public List<TraitsValuePair> getRootTraitList(Long language);

}
