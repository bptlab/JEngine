package de.hpi.bpt.chimera.model.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import de.hpi.bpt.chimera.model.Listable;
import de.hpi.bpt.chimera.model.Nameable;

@Entity
public class ObjectLifecycleState implements Nameable, Listable {
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	private int dbId;

	private String name;
	// TODO: rework this so that the same information isn't saved twice
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "objectlifecyclestate_successors")
	private List<ObjectLifecycleState> successors;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "objectlifecyclestate_predecessors")
	private List<ObjectLifecycleState> predecessors;

	public ObjectLifecycleState() {
		this.successors = new ArrayList<>();
		this.predecessors = new ArrayList<>();
	}

	/**
	 * Check if this ObjectLifecycleState is a successor of the supposed
	 * preceding ObjectLifecycleState.
	 * 
	 * @param precedingObjectLifecycleState
	 * @return true if this ObjectLifecycleState is a successor of the supposed
	 *         preceding ObjectLifecycleState
	 */
	public boolean isSuccessorOf(ObjectLifecycleState precedingObjectLifecycleState) {
		return precedingObjectLifecycleState.getSuccessors().contains(this) && this.getPredecessors().contains(precedingObjectLifecycleState);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setSuccessors(List<ObjectLifecycleState> successors) {
		this.successors = successors;
	}

	public List<ObjectLifecycleState> getSuccessors() {
		return this.successors;
	}

	public void setPredecessors(List<ObjectLifecycleState> predecessors) {
		this.predecessors = predecessors;
	}

	public List<ObjectLifecycleState> getPredecessors() {
		return this.predecessors;
	}

	public void addSuccessor(ObjectLifecycleState successor) {
		successors.add(successor);
	}

	public void addPredecessor(ObjectLifecycleState predecessor) {
		predecessors.add(predecessor);
	}
}
