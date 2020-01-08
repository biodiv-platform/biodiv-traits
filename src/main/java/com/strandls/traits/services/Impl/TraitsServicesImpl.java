/**
 * 
 */
package com.strandls.traits.services.Impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.taxonomy.controllers.TaxonomyServicesApi;
import com.strandls.traits.dao.FactsDAO;
import com.strandls.traits.dao.TraitTaxonomyDefinitionDao;
import com.strandls.traits.dao.TraitsValueDao;
import com.strandls.traits.pojo.FactValuePair;
import com.strandls.traits.pojo.Facts;
import com.strandls.traits.pojo.TraitTaxonomyDefinition;
import com.strandls.traits.pojo.Traits;
import com.strandls.traits.pojo.TraitsValue;
import com.strandls.traits.pojo.TraitsValuePair;
import com.strandls.traits.services.TraitsServices;

/**
 * @author Abhishek Rudra
 *
 */
public class TraitsServicesImpl implements TraitsServices {

	private final Logger logger = LoggerFactory.getLogger(TraitsServicesImpl.class);

	@Inject
	private FactsDAO factsDao;

	@Inject
	private TraitTaxonomyDefinitionDao traitTaxonomyDef;

	@Inject
	private TaxonomyServicesApi taxonomyService;

	@Inject
	private TraitsValueDao traistValueDao;

	@Override
	public List<FactValuePair> getFacts(String objectType, Long objectId) {
		List<FactValuePair> facts = factsDao.getTraitValuePair(objectType, objectId);
		return facts;
	}

	@Override
	public FactValuePair getFactIbp(Long id) {
		FactValuePair fact = factsDao.getTraitvaluePairIbp(id);
		return fact;
	}

	@Override
	public List<TraitsValuePair> getTraitList(Long speciesId) {
		List<TraitTaxonomyDefinition> taxonList = traitTaxonomyDef.findAllTraitList("8,9,10,11,12,13"); // trait id
		List<Long> rootTrait = traitTaxonomyDef.findAllRootTrait();
		Set<Long> traitSet = new TreeSet<Long>();
		List<TraitsValuePair> traitValuePair = new ArrayList<TraitsValuePair>();
		try {
			if (speciesId == 829 || speciesId == 830) {
				traitSet.addAll(traitTaxonomyDef.findAllObservationTrait());
			} else {
				List<String> taxonomyList = new ArrayList<String>();
				for (TraitTaxonomyDefinition ttd : taxonList) {
					taxonomyList.add(ttd.getTaxonomyDefifintionId().toString());
				}
				List<String> resultList = taxonomyService.getTaxonomyBySpecies(speciesId.toString(), taxonomyList);

				for (String result : resultList) {
					for (TraitTaxonomyDefinition ttd : taxonList) {
						if (ttd.getTaxonomyDefifintionId().toString().equals(result)) {
							traitSet.add(ttd.getTraitTaxonId());
						}
					}
				}
			}

			for (Long trait : rootTrait) {
				traitSet.add(trait);
			}

			Map<Traits, List<TraitsValue>> traitValueMap = traitTaxonomyDef.findTraitValueList(traitSet);

			TreeMap<Traits, List<TraitsValue>> sorted = new TreeMap<Traits, List<TraitsValue>>(
					new Comparator<Traits>() {

						@Override
						public int compare(Traits o1, Traits o2) {
							if (o1.getId() < o2.getId())
								return -1;
							return 1;
						}
					});
			sorted.putAll(traitValueMap);

			for (Traits traits : sorted.keySet()) {
				traitValuePair.add(new TraitsValuePair(traits, traitValueMap.get(traits)));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return traitValuePair;

	}

	@Override
	public List<FactValuePair> createFacts(HttpServletRequest request, String objectType, Long objectId,
			List<FactValuePair> factsList) {

		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		String userName = profile.getUsername();
		Long userId = Long.parseLong(profile.getId());
		List<FactValuePair> failedList = new ArrayList<FactValuePair>();
		for (FactValuePair factValue : factsList) {
			if (factValue.getNameId().equals(traistValueDao.findById(factValue.getValueId()).getTraitInstanceId())) {
				Facts fact = new Facts(null, 0L, userName, userId, false, 822L, objectId, null, factValue.getNameId(),
						factValue.getValueId(), null, objectType, null, null, null, null);
				Facts result = factsDao.save(fact);
				if (result == null)
					failedList.add(factValue);
			} else
				factsList.add(factValue);
		}

		return failedList;

	}

	@Override
	public List<Facts> fetchByTaxonId(Long taxonId) {
		List<Facts> result = factsDao.findByTaxonId(taxonId);
		return result;
	}

	@Override
	public List<TraitsValue> fetchTraitsValue(Long traitId) {
		List<TraitsValue> result = traistValueDao.findTraitsValue(traitId);
		return result;
	}

	@Override
	public List<FactValuePair> updateTraits(HttpServletRequest request, String objectType, Long objectId, Long traitId,
			List<Long> traitsValueList) {

		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		Long userId = Long.parseLong(profile.getId());
		String userName = profile.getUsername();
		List<Long> previousValueId = new ArrayList<Long>();

		List<Facts> previousFacts = factsDao.fetchByTraitId(objectType, objectId, traitId);
		for (Facts fact : previousFacts) {
			if (!(traitsValueList.contains(fact.getTraitValueId()))) {
				factsDao.delete(fact);
			}
			previousValueId.add(fact.getTraitValueId());
		}
		for (Long newValue : traitsValueList) {
			if (!(previousValueId.contains(newValue))) {
				Facts fact = new Facts(null, 0L, userName, userId, false, 822L, objectId, null, traitId, newValue, null,
						objectType, null, null, null, null);
				factsDao.save(fact);
			}
		}
		List<FactValuePair> result = getFacts(objectType, objectId);

		return result;
	}

}
