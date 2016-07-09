package hu.infokristaly.archiwar.middle.services;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.primefaces.model.SortOrder;

public abstract class BasicService<T> {

    @Inject
    protected EntityManager em;

    @Inject
    protected Logger log;
    
	public void persist(T item) {
		em.persist(item);
	}

	public T merge(T item) {
        return em.merge(item);
	}
	
	public void remove(T item) {		
		em.remove(item);
	}
	
	public T find(Class<T> type, Long primaryKey) {		
		T item = (T) em.find(type, primaryKey);
		return item;
	}
	
	protected abstract String buildQueryString(Map<String, Object> filters);
	protected abstract Query buildCountQuery(Map<String, Object> filters);
	protected abstract Query buildQuery(String sortField, SortOrder sortOrder, Map<String, Object> filters);
	protected abstract void setQueryParams(Query q, Map<String, Object> filters);
	
	@SuppressWarnings("unchecked")
	public List<T> findRange(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
		List<T> result = null;
		Query q = buildQuery(sortField,sortOrder,filters);
		setQueryParams(q,filters);		
		q.setFirstResult(first);
		q.setMaxResults(pageSize);
		result = q.getResultList();
		return result;
	}
	
	public int count(Map<String, Object> actualFilters) {
		int result = 0;
		Query q = buildCountQuery(actualFilters);
		result = ((Long)q.getSingleResult()).intValue();
		return result;
	}

}
