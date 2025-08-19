/**
 * 
 */
package com.strandls.traits.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.traits.pojo.TraitTaxonomyDefinition;
import com.strandls.traits.util.AbstractDAO;

import jakarta.inject.Inject;

/**
 * @author Abhishek Rudra
 *
 */
public class TraitTaxonomyDefinitionDao extends AbstractDAO<TraitTaxonomyDefinition, Long> {

	private final Logger logger = LoggerFactory.getLogger(TraitTaxonomyDefinitionDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected TraitTaxonomyDefinitionDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public TraitTaxonomyDefinition findById(Long id) {
		Session session = sessionFactory.openSession();
		TraitTaxonomyDefinition entity = null;
		try {
			entity = session.get(TraitTaxonomyDefinition.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<TraitTaxonomyDefinition> findAllByTraitList(List<Long> traitList) {
		String qry = "from TraitTaxonomyDefinition where traitTaxonId in :traitList";
		List<TraitTaxonomyDefinition> result = new ArrayList<TraitTaxonomyDefinition>();
		Session session = sessionFactory.openSession();
		try {
			Query<TraitTaxonomyDefinition> query = session.createQuery(qry);
			query.setParameter("traitList", traitList);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<TraitTaxonomyDefinition> findAllByTaxonomyList(List<Long> taxonomyList) {
		String qry = "from TraitTaxonomyDefinition where taxonomyDefifintionId in :taxonList";
		List<TraitTaxonomyDefinition> result = new ArrayList<TraitTaxonomyDefinition>();
		Session session = sessionFactory.openSession();
		try {
			Query<TraitTaxonomyDefinition> query = session.createQuery(qry);
			query.setParameter("taxonList", taxonomyList);
			result = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Long> findAllObservationRootTrait() {
		String qry = "select t.traitId from Traits t left join TraitTaxonomyDefinition ttd on ttd.traitTaxonId = t.traitId where t.showInObservation = true and ttd.taxonomyDefifintionId is NULL and t.isDeleted = FALSE";
		Session session = sessionFactory.openSession();
		List<Long> resultList = new ArrayList<Long>();

		try {
			Query<Long> query = session.createQuery(qry);
			resultList = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<Long> findAllSpeciesRootTraits() {
		String qry = "select t.traitId from Traits t left join TraitTaxonomyDefinition ttd on ttd.traitTaxonId = t.traitId where t.isNotObservationTraits = true and ttd.taxonomyDefifintionId is NULL and t.isDeleted = FALSE";
		Session session = sessionFactory.openSession();
		List<Long> resultList = new ArrayList<Long>();

		try {
			Query<Long> query = session.createQuery(qry);
			resultList = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return resultList;

	}

}
