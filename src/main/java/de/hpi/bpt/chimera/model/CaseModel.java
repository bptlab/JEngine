package de.hpi.bpt.chimera.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.Transient;

import de.hpi.bpt.chimera.model.condition.TerminationCondition;
import de.hpi.bpt.chimera.model.datamodel.DataModel;
import de.hpi.bpt.chimera.model.fragment.Fragment;
import de.hpi.bpt.chimera.persistencemanager.DomainModelPersistenceManager;

@Entity
public class CaseModel {


	// TODO is this id unique, or should we add an auto generated id which then
	// is for the database only?
	@Id
	private String id;
	private String name;
	private int versionNumber;
	@OneToOne(cascade = CascadeType.PERSIST)
	private DataModel dataModel;
	@OneToOne(cascade = CascadeType.PERSIST)
	private TerminationCondition terminationCondition;
	@OneToMany(cascade = CascadeType.PERSIST)
	private List<Fragment> fragments;


	/**
	 * Persists a the CaseModel to the database using the Java Persistence API
	 * "EclipseLink".
	 */
	public void saveCaseModel() {
		EntityManager entityManager = DomainModelPersistenceManager.getEntityManagerFactory().createEntityManager();

		entityManager.getTransaction().begin();
		entityManager.merge(this);
		entityManager.getTransaction().commit();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	public DataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	public TerminationCondition getTerminationCondition() {
		return terminationCondition;
	}

	public void setTerminationCondition(TerminationCondition terminationCondition) {
		this.terminationCondition = terminationCondition;
	}

	public List<Fragment> getFragments() {
		return fragments;
	}

	public void setFragments(List<Fragment> fragments) {
		this.fragments = fragments;
	}
}
