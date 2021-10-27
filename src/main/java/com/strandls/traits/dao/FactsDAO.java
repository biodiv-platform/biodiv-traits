/**
 * 
 */
package com.strandls.traits.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.traits.pojo.FactValuePair;
import com.strandls.traits.pojo.Facts;
import com.strandls.traits.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class FactsDAO extends AbstractDAO<Facts, Long> {

	private static final Logger logger = LoggerFactory.getLogger(FactsDAO.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected FactsDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Facts findById(Long id) {
		Session session = sessionFactory.openSession();
		Facts entity = null;
		try {
			entity = session.get(Facts.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public FactValuePair getTraitvaluePairIbp(Long factId) {
		String qry = "select t.id, t.name , v.id, v.value, t.traitTypes, t.isParticipatory, f.value, f.toValue, f.fromDate, f.toDate from Facts f "
				+ "left join Traits t on f.traitInstanceId = t.id "
				+ "left join TraitsValue v on f.traitValueId = v.id " + "where f.id = :id";
		Session session = sessionFactory.openSession();
		FactValuePair fact = null;
		try {
			Query<Object[]> query = session.createQuery(qry);
			query.setParameter("id", factId);

			Object[] result = query.getSingleResult();
			String value = null;

			Date fromDate = null;
			Date toDate = null;
			if (result[3] == null && result[6] == null) {
				String fDate = result[8].toString();
				String tDate = result[9].toString();
				String pattern = "yyyy-MM-dd";
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				if (fDate != null)
					fromDate = sdf.parse(fDate);
				if (tDate != null)
					toDate = sdf.parse(tDate);
			} else if (result[3] == null) {
				value = result[6].toString() + (result[7] != null ? ":" + result[7].toString() : "");
			}

			fact = new FactValuePair(Long.parseLong(result[0].toString()), result[1].toString(),
					(result[2] != null) ? Long.parseLong(result[2].toString()) : null,
					(result[3] != null) ? result[3].toString() : value, fromDate, toDate, result[4].toString(),
					Boolean.parseBoolean(result[5].toString()));
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return fact;
	}

	@SuppressWarnings("unchecked")
	public List<FactValuePair> getTraitValuePair(String objectType, Long objectId, Long traitId) {

		String qry = "select t.id, t.name , v.id, v.value, t.traitTypes, t.isParticipatory, f.value, f.toValue, f.fromDate, f.toDate from Facts f "
				+ "left join Traits t on f.traitInstanceId = t.id "
				+ "left join TraitsValue v on f.traitValueId = v.id "
				+ "where f.objectId = :id and f.objectType = :type";

		if (traitId != null)
			qry = qry + " and f.traitInstanceId = :traitId";

		Session session = sessionFactory.openSession();
		List<FactValuePair> fact = null;

		try {
			Query<Object[]> query = session.createQuery(qry);
			query.setParameter("id", objectId);
			query.setParameter("type", objectType);
			if (traitId != null)
				query.setParameter("traitId", traitId);

			List<Object[]> resultList = new ArrayList<Object[]>();
			resultList = query.getResultList();
			fact = new ArrayList<FactValuePair>();
			for (Object[] result : resultList) {

				String value = null;
				Date fromDate = null;
				Date toDate = null;
				if (result[3] == null && result[6] == null) {
					String fDate = result[8].toString();
					String tDate = result[9].toString();
					String pattern = "yyyy-MM-dd";
					SimpleDateFormat sdf = new SimpleDateFormat(pattern);
					if (fDate != null)
						fromDate = sdf.parse(fDate);
					if (tDate != null)
						toDate = sdf.parse(tDate);
				}

				else if (result[3] == null) {
					value = result[6].toString() + (result[7] != null ? ":" + result[7].toString() : "");
				}

				FactValuePair fvp = new FactValuePair(Long.parseLong(result[0].toString()), result[1].toString(),
						(result[2] != null) ? Long.parseLong(result[2].toString()) : null,
						(result[3] != null) ? result[3].toString() : value, fromDate, toDate, result[4].toString(),
						Boolean.parseBoolean(result[5].toString()));
				fact.add(fvp);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return fact;
	}

	@SuppressWarnings("unchecked")
	public List<Facts> findByTaxonId(Long taxonId) {
		String qry = "from Facts f where f.pageTaxonId = :taxonId";
		Session session = sessionFactory.openSession();
		List<Facts> resultList = null;
		try {
			Query<Facts> query = session.createQuery(qry);
			query.setParameter("taxonId", taxonId);
			resultList = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<Facts> fetchByTraitId(String objectType, Long objectId, Long traitId) {
		String qry = "from Facts where objectId = :id and objectType = :type and traitInstanceId = :traitId and isDeleted = false";
		Session session = sessionFactory.openSession();
		List<Facts> entity = null;
		try {
			Query<Facts> query = session.createQuery(qry);
			query.setParameter("id", objectId);
			query.setParameter("type", objectType);
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
	public List<Facts> fetchByValueList(List<Long> valueList) {
		String qry = "from Facts where traitValueId in :valueList";
		Session session = sessionFactory.openSession();
		List<Facts> result = null;
		try {
			Query<Facts> query = session.createQuery(qry);
			query.setParameter("valueList", valueList);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public Long getObservationAuthor(String observationId) {
		String qry = "SELECT author_id from observation where id =" + observationId;
		Session session = sessionFactory.openSession();
		try {
			Query<Object> query = session.createNativeQuery(qry);
			Object resultObject = query.getSingleResult();
			Long authorId = Long.parseLong(resultObject.toString());
			return authorId;
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}

}
