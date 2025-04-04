/**
 * 
 */
package com.strandls.traits.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.traits.pojo.Traits;
import com.strandls.traits.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class TraitsDao extends AbstractDAO<Traits, Long> {

	private final Logger logger = LoggerFactory.getLogger(TraitsDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected TraitsDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Traits findById(Long id) {
		Session session = sessionFactory.openSession();
		Traits result = null;
		try {
			result = session.get(Traits.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Long> findAllObservationTrait() {
		String qry = "select t.traitId from Traits t where showInObservation = TRUE and isDeleted = FALSE";
		Session session = sessionFactory.openSession();
		List<Long> result = new ArrayList<Long>();
		try {
			Query<Long> query = session.createQuery(qry);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Long> findSpeciesTraitFromList(Set<Long> traitList) {
		String qry = "select traitId from Traits where showInObservation = FALSE and isDeleted = FALSE and id in :traitList";
		Session session = sessionFactory.openSession();
		List<Long> result = new ArrayList<Long>();
		try {
			Query<Long> query = session.createQuery(qry);
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
	public List<Long> findAllSpeciesTraits() {
		String qry = "select traitId from Traits where showInObservation = FALSE and isDeleted = FALSE";
		Session session = sessionFactory.openSession();
		List<Long> result = new ArrayList<Long>();
		try {
			Query<Long> query = session.createQuery(qry);
			result = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Long> findAllTraitsList() {
		String qry = "select traitId from Traits where isDeleted = FALSE";
		Session session = sessionFactory.openSession();
		List<Long> result = new ArrayList<Long>();
		try {
			Query<Long> query = session.createQuery(qry);
			result = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Traits> findTraitByTraitIdAndLanguageId(Long traitId, Long languageId) {
		String qry = "from Traits t where t.traitId = :traitId and t.languageId = :languageId";
		Session session = sessionFactory.openSession();
		List<Traits> result = new ArrayList<Traits>();
		try {
			Query<Traits> query = session.createQuery(qry);
			query.setParameter("traitId", traitId);
			query.setParameter("languageId", languageId);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Traits> findTraitByTraitId(Long traitId) {
		String qry = "from Traits t where t.traitId = :traitId";
		Session session = sessionFactory.openSession();
		List<Traits> result = new ArrayList<Traits>();
		try {
			Query<Traits> query = session.createQuery(qry);
			query.setParameter("traitId", traitId);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Traits> findTraitNames() {
		String qry = "from Traits t WHERE t.showInObservation = FALSE and t.isDeleted = FALSE ORDER BY t.name ASC";
		Session session = sessionFactory.openSession();
		List<Traits> result = new ArrayList<Traits>();
		try {
			Query<Traits> query = session.createQuery(qry);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

}
