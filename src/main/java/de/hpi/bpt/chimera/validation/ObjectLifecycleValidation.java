package de.hpi.bpt.chimera.validation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.hpi.bpt.chimera.model.datamodel.ObjectLifecycleState;
import de.hpi.bpt.chimera.parser.datamodel.SequenceFlow;

public class ObjectLifecycleValidation {

	private ObjectLifecycleValidation() {
	}

	/**
	 * Validate that every sequence flow refers to existing states.
	 * 
	 * @param mapIdToState
	 * @param sequenceFlows
	 */
	public static void validateSequenceFlow(Map<String, ObjectLifecycleState> mapIdToState,
			List<SequenceFlow> sequenceFlows) {
		// All state transition edges have to connect known OLC states
		for (SequenceFlow sequenceFlow : sequenceFlows) {
			if (!mapIdToState.containsKey(sequenceFlow.getSource())
					|| !mapIdToState.containsKey(sequenceFlow.getTarget())) {
				throw new IllegalArgumentException("Sequence flow contains unknown state id");
			}
		}
	}

	/**
	 * Validate that there is exactly one initial state (having no predecessors).
	 */
	public static void validateInitialState(Collection<ObjectLifecycleState> olcStates) {
		if (olcStates.stream().filter(olcState -> olcState.getPredecessors().isEmpty()).count() != 1) {
			throw new IllegalArgumentException("There needs to be exactly one initial state (having no predecessors)");
		}
	}
}
