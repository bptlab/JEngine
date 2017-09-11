package de.hpi.bpt.chimera.execution;

import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.execution.activity.AbstractActivityInstance;
import de.hpi.bpt.chimera.execution.activity.TaskInstance;
import de.hpi.bpt.chimera.execution.event.StartEventInstance;
import de.hpi.bpt.chimera.model.fragment.bpmn.AbstractControlNode;
import de.hpi.bpt.chimera.model.fragment.bpmn.Activity;
import de.hpi.bpt.chimera.model.fragment.bpmn.StartEvent;

public class ControlNodeInstanceFactory {
	private static Logger log = Logger.getLogger(ControlNodeInstanceFactory.class);

	private ControlNodeInstanceFactory() {
	}

	/**
	 * Create a ControlNodeInstance of specific ControlNode. Therefore check
	 * Class of ControlNode.
	 * 
	 * @param controlNode
	 * @param caseExecutioner
	 * @param fragmentInstance
	 * @return ControlNodeInstance
	 */
	public static ControlNodeInstance createControlNodeInstance(AbstractControlNode controlNode, FragmentInstance fragmentInstance) {
		Class<? extends AbstractControlNode> clazz = controlNode.getClass();
		if (clazz.equals(StartEvent.class)) {
			return new StartEventInstance((StartEvent) controlNode, fragmentInstance);
		} else if (controlNode instanceof Activity) {
			return new TaskInstance((Activity) controlNode, fragmentInstance);
		} else {
			log.error(String.format("Illegal type of ControlNode: %s", clazz.getName()));
			return null;
		}
	}
}