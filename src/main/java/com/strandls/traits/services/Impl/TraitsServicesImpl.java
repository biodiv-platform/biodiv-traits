/**
 * 
 */
package com.strandls.traits.services.Impl;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.strandls.activity.pojo.MailData;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.taxonomy.controllers.SpeciesServicesApi;
import com.strandls.taxonomy.controllers.TaxonomyTreeServicesApi;
import com.strandls.taxonomy.controllers.TaxonomyServicesApi;
import com.strandls.esmodule.ApiException;
import com.strandls.esmodule.controllers.EsServicesApi;
import com.strandls.taxonomy.pojo.BreadCrumb;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
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
import com.strandls.traits.pojo.TraitsCreateData;
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
	private TaxonomyServicesApi taxonomyService;

	@Inject
	private EsServicesApi esService;

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
		Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, true, (long) 193);

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

	public List<Traits> getAllTraitsNames() {

		List<Traits> allTraits = traitsDao.findTraitNames();

		return allTraits;
	}

	@Override
	public List<TraitsValuePair> getAllTraits(Long language) {

		Set<Long> traitSet = new TreeSet<Long>();
		List<TraitsValuePair> traitValuePair = new ArrayList<TraitsValuePair>();
		List<Long> speciesTraits = traitsDao.findAllTraitsList();

		traitSet.addAll(speciesTraits);
		Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, false, language);

		TreeMap<Traits, List<TraitsValue>> sorted = new TreeMap<Traits, List<TraitsValue>>(new Comparator<Traits>() {

			@Override
			public int compare(Traits o1, Traits o2) {
				if (o1.getTraitId() < o2.getTraitId())
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
	public List<TraitsValuePair> getAllSpeciesTraits(Long language) {

		Set<Long> traitSet = new TreeSet<Long>();
		List<TraitsValuePair> traitValuePair = new ArrayList<TraitsValuePair>();
		List<Long> speciesTraits = traitsDao.findAllSpeciesTraits();

		traitSet.addAll(speciesTraits);
		Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, false, language);

		TreeMap<Traits, List<TraitsValue>> sorted = new TreeMap<Traits, List<TraitsValue>>(new Comparator<Traits>() {

			@Override
			public int compare(Traits o1, Traits o2) {
				if (o1.getTraitId() < o2.getTraitId())
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
	public List<TraitsValuePair> getSpeciesTraits(Long taxonId, Long language) {

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

			Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, false, language);

			TreeMap<Traits, List<TraitsValue>> sorted = new TreeMap<Traits, List<TraitsValue>>(
					new Comparator<Traits>() {

						@Override
						public int compare(Traits o1, Traits o2) {
							if (o1.getTraitId() < o2.getTraitId())
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
	public List<TraitsValuePair> getObservationTraitList(Long speciesGroupId, Long languageId) {
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

			Map<Traits, List<TraitsValue>> traitValueMap = traitsValueDao.findTraitValueList(traitSet, true,
					languageId);

			TreeMap<Traits, List<TraitsValue>> sorted = new TreeMap<Traits, List<TraitsValue>>(
					new Comparator<Traits>() {

						@Override
						public int compare(Traits o1, Traits o2) {
							if (o1.getTraitId() < o2.getTraitId())
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
	public String updateTraits(Long id, List<TraitsCreateData> translations) {
		List<Long> traitValueIds = new ArrayList<>();
		for (TraitsCreateData translation : translations) {
			if (translation.getTraits().getId() != null) {
				Traits trait = traitsDao.findById(translation.getTraits().getId());
				trait.setDescription(translation.getTraits().getDescription());
				trait.setName(translation.getTraits().getName());
				trait.setTraitTypes(translation.getTraits().getTraitTypes());
				trait.setShowInObservation(translation.getTraits().getShowInObservation());
				trait.setIsNotObservationTraits(translation.getTraits().getIsNotObservationTraits());
				trait.setIsParticipatory(translation.getTraits().getIsParticipatory());
				trait.setSource(translation.getTraits().getSource());
				trait.setLastRevised(new Date());
				traitsDao.update(trait);
			} else {
				Traits traits = new Traits();
				traits.setId(null);
				traits.setCreatedOn(new Date());
				traits.setDataType(translation.getTraits().getDataType());
				traits.setDescription(translation.getTraits().getDescription());
				traits.setFieldId(translation.getTraits().getFieldId());
				traits.setName(translation.getTraits().getName());
				traits.setTraitTypes(translation.getTraits().getTraitTypes());
				traits.setLastRevised(new Date());
				traits.setUnits(translation.getTraits().getUnits());
				traits.setIsNotObservationTraits(translation.getTraits().getIsNotObservationTraits());
				traits.setShowInObservation(translation.getTraits().getShowInObservation());
				traits.setIsParticipatory(translation.getTraits().getIsParticipatory());
				traits.setIsDeleted(false);
				traits.setSource(translation.getTraits().getSource());
				traits.setIcon(translation.getTraits().getIcon());
				traits.setTraitId(id);
				traits.setLanguageId(translation.getTraits().getLanguageId());
				traits = traitsDao.save(traits);
			}
			Long index = (long) 0;
			for (TraitsValue value : translation.getValues()) {
				traitValueIds.add(value.getTraitValueId());
				if (value.getTraitValueId() != null) {
					if (value.getId() != null) {
						TraitsValue traitValue = traitsValueDao.findById(value.getId());
						traitValue.setValue(value.getValue());
						traitValue.setDisplayOrder(index);
						traitValue.setSource(translation.getTraits().getSource());
						traitValue.setDescription(value.getDescription());
						traitValue.setIcon(value.getIcon());
						traitsValueDao.update(traitValue);
					} else {
						TraitsValue traitsValue = new TraitsValue();
						traitsValue.setId(null);
						traitsValue.setValue(value.getValue());
						traitsValue.setSource(translation.getTraits().getSource());
						traitsValue.setIcon(value.getIcon());
						traitsValue.setIsDeleted(false);
						traitsValue.setTraitInstanceId(id);
						traitsValue.setDescription(value.getDescription());
						traitsValue.setDisplayOrder(index);
						traitsValue.setLanguageId(value.getLanguageId());
						traitsValue.setTraitValueId(value.getTraitValueId());
						traitsValue = traitsValueDao.save(traitsValue);
					}
				} else {
					TraitsValue traitsValue = new TraitsValue();
					traitsValue.setId(null);
					traitsValue.setValue(value.getValue());
					traitsValue.setSource(translation.getTraits().getSource());
					traitsValue.setIcon(value.getIcon());
					traitsValue.setIsDeleted(false);
					traitsValue.setTraitInstanceId(id);
					traitsValue.setDescription(value.getDescription());
					traitsValue.setDisplayOrder(index);
					traitsValue.setLanguageId(value.getLanguageId());
					if (traitValueIds.get(index.intValue()) != null) {
						traitsValue.setTraitValueId(traitValueIds.get(index.intValue()));
					}
					traitsValue = traitsValueDao.save(traitsValue);
					if (traitValueIds.get(index.intValue()) == null) {
						traitValueIds.set(index.intValue(), traitsValue.getId());
						traitsValue.setTraitValueId(traitValueIds.get(index.intValue()));
						traitsValueDao.update(traitsValue);
					}
				}
				index = index + 1;
			}
		}
		List<TraitTaxonomyDefinition> existingTaxon = traitTaxonomyDef.findAllByTraitList(Arrays.asList(id));
		for (TraitTaxonomyDefinition taxonDetails : existingTaxon) {
			traitTaxonomyDef.delete(taxonDetails);
		}
		for (TraitTaxonomyDefinition taxon : translations.get(0).getQuery()) {
			TraitTaxonomyDefinition taxonId = new TraitTaxonomyDefinition();
			taxonId.setTraitTaxonId(id);
			taxonId.setTaxonomyDefifintionId(taxon.getTaxonomyDefifintionId());
			traitTaxonomyDef.save(taxonId);
		}
		return translations.toString();
	}

	@Override
	public String createTraits(List<TraitsCreateData> traitsCreateData) {
		Long traitId = null;
		List<Long> traitValueIds = new ArrayList<>();
		for (TraitsCreateData traitData : traitsCreateData) {
			Traits traits = new Traits();
			traits.setId(null);
			traits.setCreatedOn(new Date());
			traits.setDataType(traitData.getTraits().getDataType());
			traits.setDescription(traitData.getTraits().getDescription());
			traits.setFieldId(traitData.getTraits().getFieldId());
			traits.setName(traitData.getTraits().getName());
			traits.setTraitTypes(traitData.getTraits().getTraitTypes());
			traits.setLastRevised(new Date());
			traits.setUnits(traitData.getTraits().getUnits());
			traits.setIsNotObservationTraits(traitData.getTraits().getIsNotObservationTraits());
			traits.setShowInObservation(traitData.getTraits().getShowInObservation());
			traits.setIsParticipatory(traitData.getTraits().getIsParticipatory());
			traits.setIsDeleted(false);
			traits.setSource(traitData.getTraits().getSource());
			traits.setIcon(traitData.getTraits().getIcon());
			if (traitId != null) {
				traits.setTraitId(traitId);
			}
			traits.setLanguageId(traitData.getTraits().getLanguageId());
			traits = traitsDao.save(traits);
			if (traitId == null) {
				traitId = traits.getId();
				traits.setTraitId(traitId);
				traitsDao.update(traits);
			}
			Long index = (long) 0;
			for (TraitsValue Value : traitData.getValues()) {
				TraitsValue traitsValue = new TraitsValue();
				traitsValue.setId(null);
				traitsValue.setValue(Value.getValue());
				traitsValue.setSource(traitData.getTraits().getSource());
				traitsValue.setIcon(Value.getIcon());
				traitsValue.setIsDeleted(false);
				traitsValue.setTraitInstanceId(traitId);
				traitsValue.setDescription(Value.getDescription());
				traitsValue.setDisplayOrder(index);
				traitsValue.setLanguageId(Value.getLanguageId());
				if (traitValueIds.size() >= (index + 1)) {
					traitsValue.setTraitValueId(traitValueIds.get(index.intValue()));
				}
				traitsValue = traitsValueDao.save(traitsValue);
				if (traitValueIds.size() < (index + 1)) {
					traitValueIds.add(traitsValue.getId());
					traitsValue.setTraitValueId(traitValueIds.get(index.intValue()));
					traitsValueDao.update(traitsValue);
				}
				index = index + 1;
			}
		}
		for (TraitTaxonomyDefinition taxon : traitsCreateData.get(0).getQuery()) {
			TraitTaxonomyDefinition taxonId = new TraitTaxonomyDefinition();
			taxonId.setTraitTaxonId(traitId);
			taxonId.setTaxonomyDefifintionId(taxon.getTaxonomyDefifintionId());
			traitTaxonomyDef.save(taxonId);
		}
		return traitId.toString();
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
				System.out.print(entry);
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
								factsCreateData.getPageTaxonId(), entry.getKey(), null, value[0].trim(), objectType,
								value[1].trim(), null, null);
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
	public Map<String, Object> fetchByTraitIdByLanguageId(Long traitId, Long languageId) {
		List<Traits> traitDetails = traitsDao.findTraitByTraitIdAndLanguageId(traitId, languageId);
		if (traitDetails.size() == 0) {
			traitDetails = traitsDao.findTraitByTraitId(traitId);
		}
		List<TraitsValue> traitValuesList = traitsValueDao.findTraitsValueByLanguage(traitId,
				traitDetails.get(0).getLanguageId());
		Map<String, Object> details = new HashMap<>();
		details.put("traits", traitDetails.get(0));
		details.put("values", traitValuesList);
		return details;
	}

	@Override
	public List<Map<String, Object>> fetchByTraitId(Long traitId) {
		List<Map<String, Object>> result = new ArrayList<>();
		List<Traits> traitDetails = traitsDao.findTraitByTraitId(traitId);
		List<TraitTaxonomyDefinition> taxon = traitTaxonomyDef.findAllByTraitList(Arrays.asList(traitId));
		List<TaxonomyDefinition> taxonRes = new ArrayList<>();
		TreeSet<Long> treeSet = new TreeSet<>();
		for (TraitTaxonomyDefinition num : taxon) {
			treeSet.add(num.getTaxonomyDefifintionId());
		}
		for (Long t : treeSet) {
			try {
				taxonRes.add(taxonomyService.getTaxonomyConceptName(t.toString()));
			} catch (com.strandls.taxonomy.ApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (Traits t : traitDetails) {
			List<TraitsValue> traitValuesList = traitsValueDao.findTraitsValueByLanguage(t.getTraitId(),
					t.getLanguageId());
			Map<String, Object> details = new HashMap<>();
			details.put("traits", t);
			details.put("values", traitValuesList);
			details.put("taxon", taxonRes);
			result.add(details);
		}
		return result;
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
	public String bulkTraitsUpdate(HttpServletRequest request, String objectType, Long objectId,
			Map<String, List> factsAddData, String userId, String taxonId) {
		try {
			Object SpeciesEs = esService.fetch("extended_species", "_doc", objectId.toString()).getDocument();
			ObjectMapper objectMapper = new ObjectMapper();

			if (SpeciesEs instanceof String) {
				// Parse the JSON string
				String speciesEsJson = (String) SpeciesEs;
				JsonNode rootNode = objectMapper.readTree(speciesEsJson);
				List<Map<String, Object>> factsEs = objectMapper.convertValue(rootNode.get("facts"),
						new TypeReference<List<Map<String, Object>>>() {
						});
				if (factsEs == null) {
					factsEs = new ArrayList<>();
				}
				for (Entry<String, List> fact : factsAddData.entrySet()) {
					Traits trait = traitsDao.findById(Long.parseLong(fact.getKey().split("\\|")[0]));
					if (trait.getDataType().equals("STRING")) {
						List<Object> traitsValueList = fact.getValue();
						if (trait.getTraitTypes().equals(TRAITTYPE.SINGLECATEGORICAL.getValue())) {
							if (traitsValueList != null && !traitsValueList.isEmpty()) {
								Object value = traitsValueList.get(0);
								traitsValueList.clear();
								traitsValueList.add(value);
							}
						}
						String attribution = trait.getSource();
						if (fact.getKey().split("\\|").length > 1) {
							attribution = fact.getKey().split("\\|")[1];
						}

//							traits with preDefined list
						List<TraitsValue> valueList = traitsValueDao.findTraitsValue(trait.getId());
						List<Long> validValueId = new ArrayList<Long>();
						for (TraitsValue tv : valueList) {
							validValueId.add(tv.getId());
						}

						List<Facts> previousFacts = factsDao.fetchByTraitId(objectType, objectId, trait.getId());
						if (previousFacts != null && !previousFacts.isEmpty()) {
							for (Facts prevfact : previousFacts) {

								if (traitsValueList != null && !traitsValueList.isEmpty()) {
									factsDao.delete(prevfact);
								}
							}
						}

						factsEs.removeIf(factEs -> trait.getId().toString().equals(factEs.get("nameId").toString()));

						String activityType = TRAITMSG.ADDEDFACT.getValue();
						if (traitsValueList != null && !traitsValueList.isEmpty()) {
							for (Object newValue : traitsValueList) {
								if (validValueId.contains(Long.valueOf(newValue.toString()))) {
									Facts new_fact = new Facts(null, attribution, Long.parseLong(userId), false, 822L,
											objectId, Long.parseLong(taxonId), trait.getId(),
											Long.valueOf(newValue.toString()), null, objectType, null, null, null);

									String value = traitsValueDao.findById(new_fact.getTraitValueId()).getValue();
									String description = trait.getName() + ":" + value;

									saveUpdateFacts(request, objectType, objectId, new_fact, description, activityType,
											null);

									Map<String, Object> EsAddFact = new LinkedHashMap<>();
									EsAddFact.put("nameId", trait.getId());
									EsAddFact.put("valueId", Long.valueOf(newValue.toString()));
									EsAddFact.put("fromDate", null);
									EsAddFact.put("name", trait.getName());
									EsAddFact.put("type", trait.getTraitTypes());
									EsAddFact.put("color", null);
									EsAddFact.put("range", null);
									EsAddFact.put("toDate", null);
									EsAddFact.put("isParticipatory", trait.getIsParticipatory());
									EsAddFact.put("value", null);
									factsEs.add(EsAddFact);
								}
							}
						}
					} else if (trait.getDataType().equalsIgnoreCase(DATATYPE.NUMERIC.getValue())
							&& fact.getValue() != null) {
						List<String> traitsValueList = fact.getValue();
						String[] values = traitsValueList.get(0).split(":");
						List<Facts> previousFacts = factsDao.fetchByTraitId(objectType, objectId, trait.getId());
						if (previousFacts != null && !previousFacts.isEmpty()) {
							for (Facts prevfact : previousFacts) {
								factsDao.delete(prevfact);
							}
						}
						factsEs.removeIf(factEs -> trait.getId().toString().equals(factEs.get("nameId").toString()));
						if (values.length == 1) {
							Facts facts = new Facts(null,
									(fact.getKey().split("\\|").length > 1) ? fact.getKey().split("\\|")[1]
											: trait.getSource(),
									Long.parseLong(userId), false, defaultLicenseId, objectId, Long.parseLong(taxonId),
									trait.getId(), null, values[0].trim(), objectType, null, null, null);
							String description = trait.getName() + ":" + traitsValueList.get(0).toString();

							saveUpdateFacts(request, objectType, objectId, facts, description,
									TRAITMSG.ADDEDFACT.getValue(), null);
							Map<String, Object> EsAddFact = new LinkedHashMap<>();
							Map<String, Object> range = new LinkedHashMap<>();
							range.put("nameId", trait.getId());
							range.put("min", values[0].trim());
							range.put("max", null);
							EsAddFact.put("nameId", trait.getId());
							EsAddFact.put("valueId", null);
							EsAddFact.put("fromDate", null);
							EsAddFact.put("name", trait.getName());
							EsAddFact.put("type", trait.getTraitTypes());
							EsAddFact.put("color", null);
							EsAddFact.put("range", range);
							EsAddFact.put("toDate", null);
							EsAddFact.put("isParticipatory", trait.getIsParticipatory());
							EsAddFact.put("value", values[0].trim());
							factsEs.add(EsAddFact);
						} else {
							Facts facts = new Facts(null,
									(fact.getKey().split("\\|").length > 1) ? fact.getKey().split("\\|")[1]
											: trait.getSource(),
									Long.parseLong(userId), false, defaultLicenseId, objectId, Long.parseLong(taxonId),
									trait.getId(), null, values[0].trim(), objectType, values[1].trim(), null, null);
							String description = trait.getName() + ":" + traitsValueList.get(0).toString();

							saveUpdateFacts(request, objectType, objectId, facts, description,
									TRAITMSG.ADDEDFACT.getValue(), null);
							Map<String, Object> EsAddFact = new LinkedHashMap<>();
							Map<String, Object> range = new LinkedHashMap<>();
							range.put("nameId", trait.getId());
							range.put("min", values[0].trim());
							range.put("max", values[1].trim());
							EsAddFact.put("nameId", trait.getId());
							EsAddFact.put("valueId", null);
							EsAddFact.put("fromDate", null);
							EsAddFact.put("name", trait.getName());
							EsAddFact.put("type", trait.getTraitTypes());
							EsAddFact.put("color", null);
							EsAddFact.put("range", range);
							EsAddFact.put("toDate", null);
							EsAddFact.put("isParticipatory", trait.getIsParticipatory());
							EsAddFact.put("value", values[0].trim());
							factsEs.add(EsAddFact);
						}
					} else if (trait.getDataType().equalsIgnoreCase(DATATYPE.DATE.getValue())) {
						List<String> traitsValueList = fact.getValue();
						List<Facts> previousFacts = factsDao.fetchByTraitId(objectType, objectId, trait.getId());
						if (previousFacts != null && !previousFacts.isEmpty()) {
							for (Facts prevfact : previousFacts) {
								factsDao.delete(prevfact);
							}
						}
						factsEs.removeIf(factEs -> trait.getId().toString().equals(factEs.get("nameId").toString()));
						String pattern = "yyyy-MM-dd";
						SimpleDateFormat sdf = new SimpleDateFormat(pattern);
						Date fromDate = sdf.parse(traitsValueList.get(0));
						Date toDate = null;
						if (traitsValueList.size() == 2) {
							toDate = sdf.parse(traitsValueList.get(1));
						}
						Facts facts = new Facts(null,
								(fact.getKey().split("\\|").length > 1) ? fact.getKey().split("\\|")[1]
										: trait.getSource(),
								Long.parseLong(userId), false, defaultLicenseId, objectId, Long.parseLong(taxonId),
								trait.getId(), null, null, objectType, null, fromDate, toDate);

						String description = trait.getName() + ":" + traitsValueList.toString();

						saveUpdateFacts(request, objectType, objectId, facts, description,
								TRAITMSG.ADDEDFACT.getValue(), null);

						SimpleDateFormat sdfEs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

						Map<String, Object> EsAddFact = new LinkedHashMap<>();
						EsAddFact.put("nameId", trait.getId());
						EsAddFact.put("valueId", null);
						EsAddFact.put("fromDate", sdfEs.format(fromDate));
						EsAddFact.put("name", trait.getName());
						EsAddFact.put("type", trait.getTraitTypes());
						EsAddFact.put("color", null);
						EsAddFact.put("range", null);
						if (traitsValueList.size() == 2) {
							EsAddFact.put("toDate", sdfEs.format(toDate));
						} else {
							EsAddFact.put("toDate", null);
						}
						EsAddFact.put("isParticipatory", trait.getIsParticipatory());
						EsAddFact.put("value", null);
						factsEs.add(EsAddFact);

					} else {
						List<String> traitsValueList = fact.getValue();
						List<Facts> previousFacts = factsDao.fetchByTraitId(objectType, objectId, trait.getId());
						if (previousFacts != null && !previousFacts.isEmpty()) {
							for (Facts prevfact : previousFacts) {

								if (traitsValueList != null && !traitsValueList.isEmpty()) {
									factsDao.delete(prevfact);
								}
							}
						}

						factsEs.removeIf(factEs -> trait.getId().toString().equals(factEs.get("nameId").toString())
								&& !traitsValueList.contains(factEs.get("value").toString()));
						String activityType = TRAITMSG.ADDEDFACT.getValue();
						if (traitsValueList != null && !traitsValueList.isEmpty()) {
							for (String newValue : traitsValueList) {
								String color = (newValue.toString());
								Facts new_fact = new Facts(null,
										(fact.getKey().split("\\|").length > 1) ? fact.getKey().split("\\|")[1]
												: trait.getSource(),
										Long.parseLong(userId), false, defaultLicenseId, objectId,
										Long.parseLong(taxonId), trait.getId(), null, color, objectType, null, null,
										null);
								String description = trait.getName() + ":" + color;

								saveUpdateFacts(request, objectType, objectId, new_fact, description, activityType,
										null);

								String[] parts = color.replace("rgb(", "").replace(")", "").split(",");

								float[] hsv = Color.RGBtoHSB(Integer.parseInt(parts[0].trim()),
										Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()), null);

								Map<String, Object> EsAddFact = new LinkedHashMap<>();
								Map<String, Object> range = new LinkedHashMap<>();
								range.put("s", hsv[1] * 100);
								range.put("h", hsv[0] * 360);
								range.put("v", hsv[2] * 100);
								range.put("nameId", trait.getId());
								EsAddFact.put("nameId", trait.getId());
								EsAddFact.put("valueId", null);
								EsAddFact.put("fromDate", null);
								EsAddFact.put("name", trait.getName());
								EsAddFact.put("type", trait.getTraitTypes());
								EsAddFact.put("color", range);
								EsAddFact.put("range", null);
								EsAddFact.put("toDate", null);
								EsAddFact.put("isParticipatory", trait.getIsParticipatory());
								EsAddFact.put("value", color);
								factsEs.add(EsAddFact);
							}
						}
					}
				}
				Map<String, Object> fields = new HashMap<>();
				fields.put("facts", factsEs);
				String updateContent = objectMapper.writeValueAsString(fields);
				esService.updateEsField("extended_species", objectId.toString(), updateContent);
				return factsEs.toString();
			}

			return SpeciesEs.toString();
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.toString();
		}

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

	@Override
	public List<Map<String, String>> importSpeciesTraits(FormDataBodyPart file, List<String> traitDetails,
			String scientificNameColumn, String taxonColumn, String speciesIdColumn, String contributorColumn,
			String attributionColumn, String licenseColumn) {
		InputStream inputStream = file.getValueAs(InputStream.class);
		try {
			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);
			List<Map<String, String>> values = new ArrayList<>();

			// Extract headers
			Row headerRow = sheet.getRow(0);
			List<String> headers = new ArrayList<>();
			Map<String, TraitsValuePair> traits = new HashMap<>();
			for (String traitDetail : traitDetails) {
				Traits trait = traitsDao.findById(Long.valueOf(traitDetail.split("\\:")[1]));
				TraitsValuePair traitValueMatch = new TraitsValuePair(trait, null);
				if (trait.getDataType().equals("STRING")) {
					List<TraitsValue> traitValues = traitsValueDao
							.findTraitsValue(Long.valueOf(traitDetail.split("\\:")[1]));
					traitValueMatch.setValues(traitValues);
				}
				traits.put(traitDetail.split("\\:")[0], traitValueMatch);
			}
			for (Cell cell : headerRow) {
				headers.add(cell.getStringCellValue());
			}
			for (Row row : sheet) {
				if (row.getRowNum() == 0) {
					continue;
				}

				Map<String, String> rowData = new LinkedHashMap<>();
				for (int i = 0; i < headers.size(); i++) {
					if (!headers.get(i).endsWith("units")) {
						if (row.getCell(i) != null && !row.getCell(i).toString().isEmpty()) {
							if (traits.containsKey(String.valueOf(i))) {
								if (traits.get(String.valueOf(i)).getTraits().getDataType().equals("STRING")) {
									String finalFact = "";
									String[] facts = row.getCell(i).toString().split(",");
									for (String fact : facts) {
										Long language = traits.get(String.valueOf(i)).getTraits().getLanguageId();
										Optional<TraitsValue> firstMatch = traits.get(String.valueOf(i)).getValues()
												.stream()
												.filter(pojo -> pojo.getValue() != null
														&& pojo.getValue().equals(fact.trim())
														&& pojo.getLanguageId().equals(language))
												.findFirst();
										if (firstMatch.isPresent()) {
											finalFact = finalFact + firstMatch.get().getTraitValueId() + "|"
													+ firstMatch.get().getValue() + "|" + firstMatch.get().getIcon()
													+ ",";
										} else {
											finalFact = finalFact + "NoMatch|" + fact + ",";
										}
									}
									rowData.put(
											headers.get(i) + "|true|STRING|"
													+ traits.get(String.valueOf(i)).getTraits().getTraitId(),
											finalFact);
								} else if (traits.get(String.valueOf(i)).getTraits().getDataType().equals("COLOR")) {
									String[] facts = row.getCell(i).toString().split(",");
									String finalFact = "";
									for (String fact : facts) {
										if (fact.charAt(0) == '#') {
											fact = fact.substring(1);
										}

										// Convert hex to RGB
										int r = Integer.parseInt(fact.substring(0, 2), 16);
										int g = Integer.parseInt(fact.substring(2, 4), 16);
										int b = Integer.parseInt(fact.substring(4, 6), 16);

										finalFact = finalFact + "rgb(" + r + ", " + g + ", " + b + ")" + "|";
									}
									rowData.put(
											headers.get(i) + "|true|COLOR|"
													+ traits.get(String.valueOf(i)).getTraits().getTraitId(),
											finalFact);
								} else {
									String headerValue = headers.get(i);
									String cellValue = row.getCell(i).toString();
									if (headers.contains(headerValue + " units")) {
										cellValue = cellValue + "|"
												+ row.getCell(headers.indexOf(headerValue + " units")).toString();
									}
									rowData.put(
											headerValue + "|true|"
													+ traits.get(String.valueOf(i)).getTraits().getDataType() + "|"
													+ traits.get(String.valueOf(i)).getTraits().getTraitId(),
											cellValue);
								}
							} else {
								if (String.valueOf(i).equals(scientificNameColumn)) {
									rowData.put("Scientific Name", row.getCell(i).toString());
								} else if (String.valueOf(i).equals(taxonColumn)) {
									rowData.put("Taxon Concept Id", row.getCell(i).toString());
								} else if (String.valueOf(i).equals(speciesIdColumn)) {
									rowData.put("Species Id", row.getCell(i).toString());
								} else if (String.valueOf(i).equals(contributorColumn)) {
									rowData.put("Contributor", row.getCell(i).toString());
								} else if (String.valueOf(i).equals(attributionColumn)) {
									rowData.put("Attribution", row.getCell(i).toString());
								} else if (String.valueOf(i).equals(licenseColumn)) {
									rowData.put("License", row.getCell(i).toString());
								} else {
									rowData.put(headers.get(i), row.getCell(i).toString());
								}
							}
						} else {
							rowData.put(headers.get(i) + (traits.containsKey(String.valueOf(i))
									? "|true|" + traits.get(String.valueOf(i)).getTraits().getTraitId()
									: ""), null);
						}
					}
				}
				values.add(rowData);
			}
			workbook.close();
			return values;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

}
