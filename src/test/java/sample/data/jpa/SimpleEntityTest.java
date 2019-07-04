package sample.data.jpa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;


/**
 * Integration test to run the application.
 *
 * @author Oliver Gierke
 * @author Jens Schauder
 */
public class SimpleEntityTest {

	EntityManager em;


	@Before
	public void before() {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("simple");
		em = emf.createEntityManager();
		em.getTransaction().begin();

		storeEntity(23L);
		storeEntity(42L);
	}

	@After
	public void after() {

		em.getTransaction().rollback();
	}

	@Test
	public void inCriteriaApiWithNonEmptyList() {
		runTestWithParameterType(Collection.class);
	}

	@Test
	public void inCriteriaApiWithNonEmptyIterable() {

		runTestWithParameterType(Iterable.class);
	}

	@Test
	public void inCriteriaApiWithNonEmptyListPlayingAround() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SimpleEntity> criteriaQuery = cb.createQuery(SimpleEntity.class);
		Root<SimpleEntity> root = criteriaQuery.from(SimpleEntity.class);
		ParameterExpression<Collection<?>> expression = (ParameterExpression<Collection<?>>)(ParameterExpression)cb.parameter( Collection.class);
		criteriaQuery.select(root).where(root.get("id").in(expression));

		TypedQuery<SimpleEntity> typedQuery = em.createQuery(criteriaQuery);


			typedQuery.setParameter(expression, Arrays.asList(23L, 42L));

		List<SimpleEntity> result = typedQuery
				.getResultList();

		assertThat(result).hasSize(2);
	}

	private void runTestWithParameterType(Class<?> parameterType) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SimpleEntity> criteriaQuery = cb.createQuery(SimpleEntity.class);
		Root<SimpleEntity> root = criteriaQuery.from(SimpleEntity.class);
		criteriaQuery.select(root).where(root.get("id").in(cb.parameter(parameterType)));

		TypedQuery<SimpleEntity> typedQuery = em.createQuery(criteriaQuery);


		Set<ParameterExpression<?>> parameters = criteriaQuery.getParameters();
		assertThat(parameters).hasSize(1);

		for (ParameterExpression parameter : parameters) {
			typedQuery.setParameter(parameter, Arrays.asList(23L, 42L));
		}


		List<SimpleEntity> result = typedQuery
				.getResultList();

		assertThat(result).hasSize(2);
	}

	private void storeEntity(long id) {

		SimpleEntity entity = new SimpleEntity();
		entity.setId(id);
		em.persist(entity);
		em.flush();
	}

}
