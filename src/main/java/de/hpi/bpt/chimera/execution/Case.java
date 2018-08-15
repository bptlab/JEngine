package de.hpi.bpt.chimera.execution;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.execution.controlnodes.ControlNodeInstance;
import de.hpi.bpt.chimera.execution.controlnodes.State;
import de.hpi.bpt.chimera.model.CaseModel;
import de.hpi.bpt.chimera.model.fragment.Fragment;

@Entity(name = "ChimeraCase")
public class Case {
	@Id
	private String id;
	private Date instantiation;
	private String name;
	@OneToOne(cascade = CascadeType.ALL)
	private CaseExecutioner caseExecutioner;
	@OneToMany(cascade = CascadeType.ALL)
	private Map<String, FragmentInstance> fragmentInstances;

	// TODO: need to make this adaptable
	private final int FRAGMENT_INSTANCES_OF_ONE_KIND_LIMIT = 100;
	private static Logger log = Logger.getLogger(Case.class);

	/**
	 * for JPA only
	 */
	public Case() {
		// JPA needs an empty constructor to instantiate objects of this class
		// at runtime.
	}


	public Case(String caseName, CaseModel caseModel, CaseExecutioner caseExecutioner) {
		this.id = UUID.randomUUID().toString().replace("-", "");
		Date date = new Date();
		this.instantiation = new java.sql.Timestamp(date.getTime());
		this.name = caseName;
		this.caseExecutioner = caseExecutioner;
		this.fragmentInstances = new HashMap<>();
		instantiate(caseModel);
	}

	/**
	 * Create Instances of all Fragments of a specific CaseModel.
	 * 
	 * @param caseModel
	 */
	private void instantiate(CaseModel caseModel) {
		for (Fragment fragment : caseModel.getFragments()) {
			addFragmentInstance(fragment);
		}
	}

	public FragmentInstance addFragmentInstance(Fragment fragment) {
		long amount = getFragmentInstances().values().stream().filter(f -> f.getFragment().equals(fragment)).count();

		if (amount < FRAGMENT_INSTANCES_OF_ONE_KIND_LIMIT) {
			FragmentInstance fragmentInstance = new FragmentInstance(fragment, this);
			fragmentInstances.put(fragmentInstance.getId(), fragmentInstance);
			return fragmentInstance;
		} else {
			log.warn("No instances of fragment %s because the maximum amount of concurrent running instances of this fragment has been reached.");
		}
		return null;
	}

	public void removeFragmentInstance(FragmentInstance fragmentInstance) {
		String fragmentInstanceId = fragmentInstance.getId();
		if (fragmentInstances.containsKey(fragmentInstanceId)) {
			fragmentInstance.getControlNodeInstances().stream()
				.filter(c -> !c.getState().equals(State.TERMINATED))
				.forEach(ControlNodeInstance::skip);
			fragmentInstance.getControlNodeIdToInstance().clear();
			fragmentInstance.getControlNodeInstanceIdToInstance().clear();
			fragmentInstances.remove(fragmentInstanceId);
		}
	}

	// GETTER & SETTER

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

	public Map<String, FragmentInstance> getFragmentInstances() {
		return fragmentInstances;
	}

	public void setFragmentInstances(Map<String, FragmentInstance> fragmentInstances) {
		this.fragmentInstances = fragmentInstances;
	}

	public CaseExecutioner getCaseExecutioner() {
		return caseExecutioner;
	}

	public void setCaseExecutioner(CaseExecutioner caseExecutioner) {
		this.caseExecutioner = caseExecutioner;
	}


	public Date getInstantiation() {
		return instantiation;
	}


	public void setInstantiation(Date instantiation) {
		this.instantiation = instantiation;
	}
}
