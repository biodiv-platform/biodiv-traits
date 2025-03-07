/**
 * 
 */
package com.strandls.traits.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import com.strandls.traits.pojo.Traits;
import com.strandls.traits.pojo.TraitsValue;
import com.strandls.traits.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class TraitsValueDao extends AbstractDAO<TraitsValue, Long> {

	private final Logger logger = LoggerFactory.getLogger(TraitsValueDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected TraitsValueDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public TraitsValue findById(Long id) {
		Session session = sessionFactory.openSession();
		TraitsValue entity = null;
		try {
			entity = session.get(TraitsValue.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<TraitsValue> findTraitsValue(Long traitId) {

		String qry = "from TraitsValue where traitInstanceId = :traitId and isDeleted = false";
		Session session = sessionFactory.openSession();
		List<TraitsValue> entity = null;
		try {
			Query<TraitsValue> query = session.createQuery(qry);
			query.setParameter("traitId", traitId);

			entity = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<TraitsValue> findTraitsValueByLanguage(Long traitId, Long languageId) {

		String qry = "from TraitsValue where traitInstanceId = :traitId and languageId = :languageId and isDeleted = false";
		Session session = sessionFactory.openSession();
		List<TraitsValue> entity = null;
		try {
			Query<TraitsValue> query = session.createQuery(qry);
			query.setParameter("traitId", traitId);
			query.setParameter("languageId", languageId);
			entity = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public Map<Traits, List<TraitsValue>> findTraitValueList(Set<Long> traitSet, Boolean isObservation, Long language) {

	    String qry = "from Traits t left join TraitsValue tv on t.traitId = tv.traitInstanceId and t.languageId = tv.languageId where t.id in (:traitSet) ";

	    if (isObservation)
	        qry = qry + "and t.showInObservation = TRUE";
	    Session session = sessionFactory.openSession();
	    List<Object[]> resultList = new ArrayList<>();
	    Map<Traits, List<TraitsValue>> traitValueMap = new HashMap<>();
	    Map<Long, Traits> seenTraitIds = new HashMap<>();
	    
	    try {
	        Query<Object[]> query = session.createQuery(qry);
	        query.setParameter("traitSet", traitSet);
	        resultList = query.getResultList();
	        
	        for (Object[] result : resultList) {
	            Traits traits = (Traits) result[0];
	            TraitsValue traitsValue = (TraitsValue) result[1];
	            Long traitId = traits.getTraitId();

	            if (!seenTraitIds.containsKey(traitId)) {
	                // First occurrence, add it
	                seenTraitIds.put(traitId, traits);
	            } else if (traits.getLanguageId().equals(language)) { // Replaced 205 with language
	                if (!seenTraitIds.get(traitId).getLanguageId().equals(language)) {
	                    traitValueMap.remove(seenTraitIds.get(traitId));
	                }
	                seenTraitIds.put(traitId, traits);
	            }

	            Traits latestTraits = seenTraitIds.get(traitId);
	            traitValueMap.computeIfAbsent(latestTraits, k -> new ArrayList<>());

	            if (traitsValue != null && latestTraits.getLanguageId().equals(traitsValue.getLanguageId())) {
	                traitValueMap.get(latestTraits).add(traitsValue);
	            }
	        }

	    } catch (Exception e) {
	        logger.error(e.getMessage());
	    } finally {
	        session.close();
	    }
	    return traitValueMap;
	}

}
