/**
 * 
 */
package com.strandls.traits.services.Impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.activity.pojo.MailData;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.taxonomy.controllers.SpeciesServicesApi;
import com.strandls.taxonomy.controllers.TaxonomyTreeServicesApi;
import com.strandls.taxonomy.pojo.BreadCrumb;
import com.strandls.traits.dao.FactsDAO;
import com.strandls.traits.dao.TraitTaxonomyDefinitionDao;
import com.strandls.traits.dao.TraitsDao;
import com.strandls.traits.dao.TraitsValueDao;
import com.strandls.traits.pojo.FactValuePair;
import com.strandls.traits.pojo.Facts;
import com.strandls.traits.pojo.FactsCreateData;
import com.strandls.traits.pojo.FactsUpdateData;
import com.strandls.traits.pojo.TraitTaxonomyDefinition;
import com.strandls.traits.pojo.Traits;
import com.strandls.traits.pojo.TraitsValue;
import com.strandls.traits.pojo.TraitsValuePair;
import com.strandls.traits.services.TraitsServices;
import com.strandls.traits.util.Constants.DATATYPE;
import com.strandls.traits.util.Constants.OBJECTTYPE;
import com.strandls.traits.util.Constants.TRAITMSG;
import com.strandls.traits.util.Constants.TRAITTYPE;
import com.strandls.traits.util.PropertyFileUtil;
import com.strandls.traits.util.TraitsException;

import net.minidev.json.JSONArray;

/**
 * @author Abhishek Rudra
 *
 */
public class TraitsServicesImpl implements TraitsServices {

	private final Logger logger = LoggerFactory.getLogger(TraitsServicesImpl.class);

	@Inject
	private LogActivities logActivity;

	@Inject
	private FactsDAO factsDao;

	@Inject
	private TraitsDao traitsDao;

	@Inject
	private TraitTaxonomyDefinitionDao traitTaxonomyDef;

	@Inject
	private SpeciesServicesApi speciesService;

	@Inject
	private TaxonomyTreeServicesApi taxonomyTreeService;

	@Inject
	private TraitsValueDao traitsValueDao;

	private Long defaultLicenseId = Long
			.parseLong(PropertyFileUtil.fetchProperty("config.properties", "defaultLicenseId"));

	@Override
	public List<FactValuePair> getFacts(String objectType, Long objectId, Long traitId) {
		return factsDao.getTraitValuePair(objectType, objectId, traitId);
	}

	@Override
	public FactValuePair getFactIbp(Long id) {
		return factsDao.getTraitvaluePairIbp(id);
	}

	@Override
	public List<TraitsValuePair> getAllObservationTraits() {

		List<TraitsValuePair> traitValuePair = new ArrayList<TraitsValuePair>();
		List<Long> allTraits = traitsDao.findAllObservationTrait();
		Set<Long> traitSet = new HashSet<Long>();
		traitSet.addAll(allTraits);
		Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, true);

		TreeMap<Traits, List<TraitsValue>> sorted = new TreeMap<Traits, List<TraitsValue>>(new Comparator<Traits>() {

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

		return traitValuePair;
	}

	@Override
	public List<TraitsValuePair> getAllSpeciesTraits() {

		Set<Long> traitSet = new TreeSet<Long>();
		List<TraitsValuePair> traitValuePair = new ArrayList<TraitsValuePair>();
		List<Long> speciesTraits = traitsDao.findAllSpeciesTraits();

		traitSet.addAll(speciesTraits);
		Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, false);

		TreeMap<Traits, List<TraitsValue>> sorted = new TreeMap<Traits, List<TraitsValue>>(new Comparator<Traits>() {

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

		return traitValuePair;

	}

	@Override
	public List<TraitsValuePair> getSpeciesTraits(Long taxonId) {

		Set<Long> traitSet = new TreeSet<Long>();
		List<TraitsValuePair> traitValuePair = new ArrayList<TraitsValuePair>();
		List<Long> taxonomyList = new ArrayList<Long>();
		try {
			List<BreadCrumb> breadCrumbs = taxonomyTreeService.getTaxonomyBreadCrumb(taxonId.toString());
			for (BreadCrumb breadCrumb : breadCrumbs) {
				taxonomyList.add(breadCrumb.getId());
			}
			// list of taxonomy id
			List<TraitTaxonomyDefinition> taxonList = traitTaxonomyDef.findAllByTaxonomyList(taxonomyList);
			for (TraitTaxonomyDefinition ttd : taxonList) {
				traitSet.add(ttd.getTraitTaxonId());
			}

//			check for is observaation false
			List<Long> filteredSpeciesTraitList = traitsDao.findSpeciesTraitFromList(traitSet);
			traitSet.clear();
			traitSet.addAll(filteredSpeciesTraitList);

//			adding the root traits
			List<Long> rootTrait = traitTaxonomyDef.findAllSpeciesRootTraits();
			traitSet.addAll(rootTrait);

//			get the values

			Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, false);

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

			return traitValuePair;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;
	}

	@Override
	public List<TraitsValuePair> getObservationTraitList(Long speciesGroupId) {
		List<Long> observationTrait = traitsDao.findAllObservationTrait();
		List<TraitTaxonomyDefinition> taxonList = traitTaxonomyDef.findAllByTraitList(observationTrait); // trait id
		List<Long> rootTrait = traitTaxonomyDef.findAllObservationRootTrait();
		Set<Long> traitSet = new TreeSet<Long>();
		List<TraitsValuePair> traitValuePair = new ArrayList<TraitsValuePair>();
		try {
			if (speciesGroupId == 829) {
				traitSet.addAll(traitsDao.findAllObservationTrait());
			} else {
				List<String> taxonomyList = new ArrayList<String>();
				for (TraitTaxonomyDefinition ttd : taxonList) {
					taxonomyList.add(ttd.getTaxonomyDefifintionId().toString());
				}
				List<String> resultList = speciesService.getTaxonomyBySpeciesGroup(speciesGroupId.toString(),
						taxonomyList);

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

			Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, true);

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
			FactsCreateData factsCreateData) {

		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			String userName = profile.getUsername();
			Long userId = Long.parseLong(profile.getId());

//			to Handle Traits with PreDefined Values
			for (Map.Entry<Long, List<Long>> entry : factsCreateData.getFactValuePairs().entrySet()) {

				Traits traits = traitsDao.findById(entry.getKey());
				List<TraitsValue> traitsValue = traitsValueDao.findTraitsValue(entry.getKey());

				for (TraitsValue values : traitsValue) {

					if (entry.getValue().contains(values.getId())) {

						String attribution = userName;
						if (objectType.equalsIgnoreCase(OBJECTTYPE.SPECIES.getValue()))
							attribution = traits.getSource();
						Facts facts = new Facts(null, attribution, userId, false, defaultLicenseId, objectId,
								factsCreateData.getPageTaxonId(), entry.getKey(), values.getId(), null, objectType,
								null, null, null);
						String description = traits.getName() + ":" + values.getValue();

						saveUpdateFacts(request, objectType, objectId, facts, description,
								TRAITMSG.ADDEDFACT.getValue(), factsCreateData.getMailData());
					}
				}
			}

//			To handle traits with User Entered Values
			for (Entry<Long, List<String>> entry : factsCreateData.getFactValueString().entrySet()) {
				Traits traits = traitsDao.findById(entry.getKey());

				String attribution = userName;
				if (objectType.equalsIgnoreCase(OBJECTTYPE.SPECIES.getValue()))
					attribution = traits.getSource();

				if (traits.getDataType().equalsIgnoreCase(DATATYPE.COLOR.getValue())) {
					for (String color : entry.getValue()) {
						Facts facts = new Facts(null, attribution, userId, false, defaultLicenseId, objectId,
								factsCreateData.getPageTaxonId(), entry.getKey(), null, color, objectType, null, null,
								null);
						String description = traits.getName() + ":" + color;

						saveUpdateFacts(request, objectType, objectId, facts, description,
								TRAITMSG.ADDEDFACT.getValue(), factsCreateData.getMailData());

					}
				} else if (traits.getDataType().equalsIgnoreCase(DATATYPE.NUMERIC.getValue())) {
					for (String range : entry.getValue()) {
						String[] value = range.split(":");
						Facts facts = new Facts(null, attribution, userId, false, defaultLicenseId, objectId,
								factsCreateData.getPageTaxonId(), entry.getKey(), null, value[0], objectType, value[1],
								null, null);
						String description = traits.getName() + ":" + range;

						saveUpdateFacts(request, objectType, objectId, facts, description,
								TRAITMSG.ADDEDFACT.getValue(), factsCreateData.getMailData());

					}

				} else if (traits.getDataType().equalsIgnoreCase(DATATYPE.DATE.getValue())) {
					for (String date : entry.getValue()) {
						String[] value = date.split(":");
						String pattern = "yyyy-MM-dd";
						SimpleDateFormat sdf = new SimpleDateFormat(pattern);
						Date fromDate = sdf.parse(value[0]);
						Date toDate = sdf.parse(value[1]);

						Facts facts = new Facts(null, attribution, userId, false, defaultLicenseId, objectId,
								factsCreateData.getPageTaxonId(), entry.getKey(), null, null, objectType, null,
								fromDate, toDate);

						String description = traits.getName() + ":" + date;

						saveUpdateFacts(request, objectType, objectId, facts, description,
								TRAITMSG.ADDEDFACT.getValue(), factsCreateData.getMailData());

					}
				}

			}

			return getFacts(objectType, objectId, null);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;

	}

	private void saveUpdateFacts(HttpServletRequest request, String objectType, Long objectId, Facts facts,
			String description, String activityType, MailData mailData) {
		Facts result = factsDao.save(facts);
		if (result != null) {
			if (objectType.equalsIgnoreCase(OBJECTTYPE.OBSERVATION.getValue()))
				logActivity.logActivity(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId, objectId,
						"observation", result.getId(), activityType, mailData);
			else if (objectType.equalsIgnoreCase(OBJECTTYPE.SPECIES.getValue()))
				logActivity.logSpeciesActivity(request.getHeader(HttpHeaders.AUTHORIZATION), description, objectId,
						objectId, "species", result.getId(), activityType, mailData);
		}
	}

	@Override
	public List<Facts> fetchByTaxonId(Long taxonId) {
		return factsDao.findByTaxonId(taxonId);
	}

	@Override
	public List<TraitsValue> fetchTraitsValue(Long traitId) {
		return traitsValueDao.findTraitsValue(traitId);
	}

	@Override
	public List<FactValuePair> updateTraits(HttpServletRequest request, String objectType, Long objectId, Long traitId,
			FactsUpdateData factsUpdateData) {

		try {
			List<Long> traitsValueList = factsUpdateData.getTraitValueList();
			List<String> valueString = factsUpdateData.getValuesString();
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			JSONArray userRole = (JSONArray) profile.getAttribute("roles");
			Long userId = Long.parseLong(profile.getId());
			String userName = profile.getUsername();

			Traits trait = traitsDao.findById(traitId);
			if (trait.getTraitTypes().equals(TRAITTYPE.SINGLECATEGORICAL.getValue())) {
				if (traitsValueList != null && !traitsValueList.isEmpty()) {
					Long value = traitsValueList.get(0);
					traitsValueList.clear();
					traitsValueList.add(value);
				}
				if (valueString != null && !valueString.isEmpty()) {
					String value = valueString.get(0);
					valueString.clear();
					valueString.add(value);
				}

			}
			String attribution = userName;
			if (objectType.equalsIgnoreCase(OBJECTTYPE.SPECIES.getValue()))
				attribution = trait.getSource();

			if (objectType.equalsIgnoreCase(OBJECTTYPE.OBSERVATION.getValue()) && !trait.getIsParticipatory()) {
				Long authorId = factsDao.getObservationAuthor(objectId.toString());
				if (!(userRole.contains("ROLE_ADMIN") || authorId.equals(userId))) {
					throw new TraitsException("User not allowed to add this traits");
				}

			}

//			traits with preDefined list
			List<TraitsValue> valueList = traitsValueDao.findTraitsValue(traitId);
			List<Long> validValueId = new ArrayList<Long>();
			for (TraitsValue tv : valueList) {
				validValueId.add(tv.getId());
			}

			List<Long> previousValueId = new ArrayList<Long>();
//			deleting previous fatcs
			List<Facts> previousFacts = factsDao.fetchByTraitId(objectType, objectId, traitId);
			if (previousFacts != null && !previousFacts.isEmpty()) {
				for (Facts fact : previousFacts) {

					if (traitsValueList != null && !traitsValueList.isEmpty()) {
						if (!(traitsValueList.contains(fact.getTraitValueId()))) {
							factsDao.delete(fact);
						}
						previousValueId.add(fact.getTraitValueId());
					} else if (valueString != null && !valueString.isEmpty()) {

						if (trait.getDataType().equalsIgnoreCase(DATATYPE.COLOR.getValue())) {
							if (!(valueString.contains(fact.getValue()))) {
								factsDao.delete(fact);
							}
						} else if (trait.getDataType().equalsIgnoreCase(DATATYPE.NUMERIC.getValue())) {
							factsDao.delete(fact);

						} else if (trait.getDataType().equalsIgnoreCase(DATATYPE.DATE.getValue())) {
							factsDao.delete(fact);
						}
					}
				}
			}

			String activityType = TRAITMSG.UPDATEDFACT.getValue();
			if (previousFacts == null || previousFacts.isEmpty())
				activityType = TRAITMSG.ADDEDFACT.getValue();

//			adding new facts
			if (traitsValueList != null && !traitsValueList.isEmpty()) {
				for (Long newValue : traitsValueList) {
					if (!(previousValueId.contains(newValue)) && validValueId.contains(newValue)) {

						Facts fact = new Facts(null, attribution, userId, false, 822L, objectId,
								factsUpdateData.getPageTaxonId(), traitId, newValue, null, objectType, null, null,
								null);

						String value = traitsValueDao.findById(fact.getTraitValueId()).getValue();
						String description = trait.getName() + ":" + value;

						saveUpdateFacts(request, objectType, objectId, fact, description, activityType,
								factsUpdateData.getMailData());

					}
				}
			} else if (valueString != null && !valueString.isEmpty()) {
				for (String value : valueString) {
					if (trait.getDataType().equalsIgnoreCase(DATATYPE.COLOR.getValue())) {
						Facts facts = new Facts(null, attribution, userId, false, defaultLicenseId, objectId,
								factsUpdateData.getPageTaxonId(), traitId, null, value.trim(), objectType, null, null,
								null);
						String description = trait.getName() + ":" + value;

						saveUpdateFacts(request, objectType, objectId, facts, description, activityType,
								factsUpdateData.getMailData());

					} else if (trait.getDataType().equalsIgnoreCase(DATATYPE.NUMERIC.getValue())) {

						String[] values = value.split(":");
						Facts facts = new Facts(null, attribution, userId, false, defaultLicenseId, objectId,
								factsUpdateData.getPageTaxonId(), traitId, null, values[0].trim(), objectType,
								values[1].trim(), null, null);
						String description = trait.getName() + ":" + value;

						saveUpdateFacts(request, objectType, objectId, facts, description, activityType,
								factsUpdateData.getMailData());

					} else if (trait.getDataType().equalsIgnoreCase(DATATYPE.DATE.getValue())) {
						String values[] = value.split(":");
						String pattern = "yyyy-MM-dd";
						SimpleDateFormat sdf = new SimpleDateFormat(pattern);
						Date fromDate = sdf.parse(values[0].trim());
						Date toDate = sdf.parse(values[1].trim());

						Facts facts = new Facts(null, attribution, userId, false, defaultLicenseId, objectId,
								factsUpdateData.getPageTaxonId(), traitId, null, null, objectType, null, fromDate,
								toDate);

						String description = trait.getName() + ":" + value;

						saveUpdateFacts(request, objectType, objectId, facts, description, activityType,
								factsUpdateData.getMailData());
					}
				}
			}

			return getFacts(objectType, objectId, traitId);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public List<Long> fetchTaxonIdByValueId(String valueList) {

		List<Long> valList = new ArrayList<Long>();

		for (String value : valueList.split(",")) {
			valList.add(Long.parseLong(value));
		}

		List<Facts> factsResult = factsDao.fetchByValueList(valList);
		List<Long> taxonList = new ArrayList<Long>();
		for (Facts fact : factsResult)
			taxonList.add(fact.getPageTaxonId());
		return taxonList;
	}

}
