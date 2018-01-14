package de.hpi.bpt.chimera.execution.event;

import de.hpi.bpt.chimera.execution.FragmentInstance;
import de.hpi.bpt.chimera.execution.State;
import de.hpi.bpt.chimera.model.fragment.bpmn.event.AbstractEvent;
import de.hpi.bpt.chimera.model.fragment.bpmn.event.IntermediateThrowEvent;

public class IntermediateThrowEventInstance extends AbstractEventInstance {

	public IntermediateThrowEventInstance(AbstractEvent event, FragmentInstance fragmentInstance) {
		super(event, fragmentInstance);
		setState(State.INIT);
	}

	@Override
	public IntermediateThrowEvent getControlNode() {
		return (IntermediateThrowEvent) super.getControlNode();
	}
}
