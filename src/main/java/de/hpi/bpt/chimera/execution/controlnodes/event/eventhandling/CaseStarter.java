package de.hpi.bpt.chimera.execution.controlnodes.event.eventhandling;

import de.hpi.bpt.chimera.execution.CaseExecutioner;
import de.hpi.bpt.chimera.execution.data.DataAttributeInstanceWriter;
import de.hpi.bpt.chimera.execution.data.DataManager;
import de.hpi.bpt.chimera.execution.data.DataObject;
import de.hpi.bpt.chimera.model.condition.AtomicDataStateCondition;
import de.hpi.bpt.chimera.model.condition.CaseStartTrigger;
import de.hpi.bpt.chimera.model.condition.CaseStartTriggerConsequence;
import de.hpi.bpt.chimera.model.datamodel.DataAttribute;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Map;

/**
 * Responsible for
 */
public class CaseStarter {
	static final Logger LOGGER = Logger.getLogger(CaseStarter.class);
	private CaseStartTrigger caseStartTrigger;

	public CaseStarter(CaseStartTrigger caseStartTrigger) {
		this.caseStartTrigger = caseStartTrigger;
	}

	public void startCase(String json, CaseExecutioner caseExecutioner) {
		// TODO: is this really necessary
		if (new JSONObject(json).length() == 0 && caseStartTrigger.hasMapping()) {
			throw new IllegalStateException("Could not initialize attributes from empty json");
		}

		initializeDataObjects(caseExecutioner, json);
		caseExecutioner.startCase();
	}

	/**
	 * This method is responsible for instantiating all data classes, which have attributes
	 * specified in the json path mapping. For each data class there will maximal be
	 * one data object.
	 */
	public void initializeDataObjects(CaseExecutioner caseExecutioner, String json) {
		DataManager dataManager = caseExecutioner.getDataManager();
		for (CaseStartTriggerConsequence triggerConsequence : caseStartTrigger.getTriggerConsequences()) {
			AtomicDataStateCondition condition = triggerConsequence.getDataObjectState();
			LOGGER.info(String.format("initialize DataObject %s of Case %s in State %s.", condition.getDataClass().getName(), caseExecutioner.getCase().getName(), condition.getObjectLifecycleState().getName()));

			DataObject dataObject = dataManager.createDataObject(condition);
			Map<DataAttribute, String> dataAttributeToJsonPath = triggerConsequence.getDataAttributeToJsonPath();
			DataAttributeInstanceWriter.writeDataAttributeInstances(dataObject, dataAttributeToJsonPath, json);
		}
	}
}

