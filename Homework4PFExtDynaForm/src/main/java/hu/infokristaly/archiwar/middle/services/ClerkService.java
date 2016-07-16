/**
 * 
 */
package hu.infokristaly.archiwar.middle.services;

import hu.infokristaly.archiwar.back.domain.Clerk;
import hu.infokristaly.archiwar.back.domain.Usergroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.primefaces.model.SortOrder;

/**
 * @author pzoli
 * 
 */
@Named
@Stateless
public class ClerkService extends BasicService<Clerk> implements Serializable {

	private static final long serialVersionUID = -2360286862757860424L;

	public Clerk find(Clerk systemUser) {
		Clerk result = em.find(Clerk.class, systemUser.getId());
		log.info("Find by Id [" + systemUser.getId() + "]"
				+ systemUser.getOsUserName() + ":" + result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Clerk> findRange(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, Object> filters) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Clerk> cq = builder.createQuery(Clerk.class);
		Root<Clerk> from = cq.from(Clerk.class);
		cq.select(from);

		if ((filters != null) && !filters.isEmpty()) {
			Set<Entry<String, Object>> es = filters.entrySet();
			List<Predicate> predicateList = new ArrayList<Predicate>();
			for (Entry<String, Object> filter : es) {
				String field = filter.getKey();
				Expression<String> x = from.get(field);
				String pattern = filter.getValue() + "%";
				x = builder.lower(x);
				pattern = pattern.toLowerCase();
				Predicate predicate = builder.like(x, pattern);
				predicateList.add(predicate);
			}

			Predicate predicates = builder.and(predicateList
					.toArray(new Predicate[predicateList.size()]));
			cq.where(predicates);

		}
		if (sortField != null) {
			if (sortOrder.equals(SortOrder.ASCENDING)) {
				cq.orderBy(builder.asc(from.get(sortField)));
			} else if (sortOrder.equals(SortOrder.DESCENDING)) {
				cq.orderBy(builder.desc(from.get(sortField)));
			}
		}
		Query q = em.createQuery(cq);
		q.setMaxResults(pageSize);
		q.setFirstResult(first);
		return q.getResultList();
	}

	/**
	 * Find All
	 * 
	 * @param first
	 *            the first
	 * @param pageSize
	 *            the page size
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public List<Clerk> findAll(boolean active) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Clerk> cq = builder.createQuery(Clerk.class);
		Root<Clerk> from = cq.from(Clerk.class);
		cq.select(from);
		if (active) {
			List<Predicate> predicateList = new ArrayList<Predicate>();

			Expression<String> x = from.get("enabled");
			Predicate predicate = builder.equal(x, true);
			predicateList.add(predicate);

			Predicate predicates = builder.and(predicateList
					.toArray(new Predicate[predicateList.size()]));
			cq.where(predicates);
		}
		cq.orderBy(builder.asc(from.get("userName")));
		Query q = em.createQuery(cq);
		return q.getResultList();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int count(Map<String, Object> actualFilters) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery cq = builder.createQuery();
		Root<Clerk> from = cq.from(Clerk.class);
		cq.select(builder.count(from));

		if ((actualFilters != null) && !actualFilters.isEmpty()) {
			Set<Entry<String, Object>> es = actualFilters.entrySet();
			List<Predicate> predicateList = new ArrayList<Predicate>();
			for (Entry<String, Object> filter : es) {
				String field = filter.getKey();
				Expression<String> x = from.get(field);
				String pattern = filter.getValue() + "%";
				x = builder.lower(x);
				pattern = pattern.toLowerCase();
				Predicate predicate = builder.like(x, pattern);
				predicateList.add(predicate);
			}

			Predicate predicates = builder.and(predicateList
					.toArray(new Predicate[predicateList.size()]));
			cq.where(predicates);

		}

		Query q = em.createQuery(cq);
		int result = ((Long) q.getSingleResult()).intValue();
		return result;
	}

	public void removeSystemUser(Clerk item) {
		item = find(item);
		em.remove(item);
	}

	@Override
	protected Query buildCountQuery(Map<String, Object> filters) {
		String result = buildQueryString(filters);
		result += "select count(*) " + result;
		Query q = em.createQuery(result);
		return q;
	}

	@Override
	protected Query buildQuery(String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		String result = buildQueryString(filters);
		Query q = em.createQuery(result);
		return q;
	}

	@Override
	protected String buildQueryString(Map<String, Object> filters) {
		StringBuffer queryBuff = new StringBuffer();
		queryBuff.append("from Clerk");
		return queryBuff.toString();
	}

	@Override
	protected void setQueryParams(Query q, Map<String, Object> filters) {

	}

	@SuppressWarnings("unchecked")
    public List<Usergroup> findGroups() {
	    Query q = em.createQuery("from Usergroup");
	    return q.getResultList();
	}

    public void persistGroup(Usergroup g) {
        em.persist(g);
    }
}
