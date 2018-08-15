package de.hpi.bpt.chimera.parser;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

import de.hpi.bpt.chimera.CaseModelTestHelper;
import de.hpi.bpt.chimera.model.CaseModel;
import de.hpi.bpt.chimera.model.condition.CaseStartTrigger;
import de.hpi.bpt.chimera.model.condition.CaseStartTriggerConsequence;
import de.hpi.bpt.chimera.model.condition.AtomicDataStateCondition;
import de.hpi.bpt.chimera.model.condition.TerminationCondition;
import de.hpi.bpt.chimera.model.condition.ConditionSet;
import de.hpi.bpt.chimera.model.datamodel.DataAttribute;
import de.hpi.bpt.chimera.model.datamodel.DataClass;
import de.hpi.bpt.chimera.model.datamodel.DataModel;
import de.hpi.bpt.chimera.model.datamodel.ObjectLifecycle;
import de.hpi.bpt.chimera.model.datamodel.ObjectLifecycleState;
import de.hpi.bpt.chimera.model.fragment.Fragment;

public class CaseModelParserTest {
	final String filepath = "src/test/resources/parser/JsonString";

	@Test
	public void parseCaseModel() {
		CaseModel caseModel;
		caseModel = CaseModelTestHelper.parseCaseModel(filepath);
		
		// assertEquals("wrong CaseModel id", "591330db1ed1325048306e40", caseModel.getId());
		assertEquals("wrong CaseModel name", "testScenario123", caseModel.getName());
		assertEquals("wrong CaseModel version", 22, caseModel.getVersionNumber());

		DataModel dataModel = caseModel.getDataModel();
		assertEquals("wrong DataModel version", 21, dataModel.getVersionNumber());

		List<DataClass> dataClasses = dataModel.getDataModelClasses();
		testDataClasses(dataClasses);

		TerminationCondition terminationCondition = caseModel.getTerminationCondition();
		testTerminationCondition(terminationCondition, dataClasses);

		List<CaseStartTrigger> caseStartTriggers = caseModel.getStartCaseTrigger();
		testStartCondition(caseStartTriggers, dataClasses);

		Fragment fragment = caseModel.getFragments().get(0);
		assertEquals("wrong Fragment id", "591330db1ed1325048306e42", fragment.getId());
		assertEquals("wrong Fragment name", "First Fragment", fragment.getName());
		assertEquals("wrong Fragment version", 3, fragment.getVersionNumber());
		// TODO: implement testing for fragment elements

		// caseModel.saveCaseModelToDB();
	}

	private void testDataClasses(List<DataClass> dataClasses) {
		assertTrue("wrong DataModelClass type: DataClass", !dataClasses.get(0).isEvent());
		DataClass dataClass = dataClasses.get(0);
		assertEquals("wrong DataClass name", "dataclass 1", dataClass.getName());

		assertTrue("wrong DataModelClass type: EventClass", dataClasses.get(2).isEvent());
		DataClass eventClass = dataClasses.get(2);
		assertEquals("wrong EventClass name", "eventclass1", eventClass.getName());

		ObjectLifecycle objectLifecycle = dataClass.getObjectLifecycle();

		testObjectLifecycleStates(objectLifecycle.getObjectLifecycleStates());

		DataAttribute dataAttribute = dataClass.getDataAttributes().get(0);
		assertEquals("wrong DataAttribute name", "testString", dataAttribute.getName());
		assertEquals("wrong DataAttribute type", "String", dataAttribute.getType());
	}

	private void testObjectLifecycleStates(List<ObjectLifecycleState> objectLifecycleStates) {
		assertTrue("wrong ObjectLifecycleStates amount", objectLifecycleStates.size() == 3);

		// has to handle olcStates in specific behavior because List is unsorted
		// because of HashMap in ObjectLifecycleParser
		ObjectLifecycleState state1 = null, state2 = null, state3 = null;
		boolean state1_occured = false, state2_occured = false, state3_occured = false;

		for (ObjectLifecycleState state : objectLifecycleStates) {
			if (state.getName().equals("State 1")) {
				state1 = state;
				state1_occured = true;
			}
			else if (state.getName().equals("State 2")) {
				state2 = state;
				state2_occured = true;
			}
			else if (state.getName().equals("State 3")) {
				state3 = state;
				state3_occured = true;
			}
		}
		assertTrue("wrong ObjectLifecycleStates", state1_occured && state2_occured && state3_occured);

		assertTrue("wrong olcState predecessors", state1.getPredecessors().isEmpty());
		assertEquals("wrong olcState successors", "State 2", state1.getSuccessors().get(0).getName());

		assertEquals("wrong olcState successors", "State 1", state2.getPredecessors().get(0).getName());
		assertTrue("wrong olcState successors", state2.getSuccessors().isEmpty());

		assertTrue("wrong olcState predecessors", state3.getPredecessors().isEmpty());
		assertTrue("wrong olcState successors", state3.getSuccessors().isEmpty());
	}

	private void testTerminationCondition(TerminationCondition terminationCondition, List<DataClass> dataClasses) {
		List<ConditionSet> components = terminationCondition.getConditionSets();
		assertEquals("wrong TerminationConditionComponent amount", 2, components.size());

		ConditionSet component1 = components.get(0);
		ConditionSet component2 = components.get(1);

		assertEquals("wrong DataObjectStateCondition amount", 2, component1.getConditions().size());
		assertEquals("wrong DataObjectStateCondition amount", 1, component2.getConditions().size());

		AtomicDataStateCondition objectStateCondition1 = component1.getConditions().get(0);
		AtomicDataStateCondition objectStateCondition2 = component1.getConditions().get(1);
		AtomicDataStateCondition objectStateCondition3 = component2.getConditions().get(0);

		DataClass dataClass = (DataClass) dataClasses.get(0);
		DataClass dc = (DataClass) dataClasses.get(1);
		ObjectLifecycleState dc_state = dc.getObjectLifecycle().getObjectLifecycleStates().get(0);
		assertTrue("wrong Dataclass mapping", objectStateCondition1.getDataClass().equals(dataClass));
		assertTrue("wrong Dataclass mapping", objectStateCondition2.getDataClass().equals(dc));
		assertTrue("wrong Olc-State mapping", objectStateCondition2.getObjectLifecycleState().equals(dc_state));
		assertTrue("wrong Dataclass mapping", objectStateCondition3.getDataClass().equals(dataClass));
		
		assertTrue("wrong Dataclass matching", objectStateCondition1.getDataClass().equals(objectStateCondition3.getDataClass()));
	}

	private void testStartCondition(List<CaseStartTrigger> caseStartTriggers, List<DataClass> dataClasses) {
		assertEquals("wrong CaseStartTrigger amount", 1, caseStartTriggers.size());
		CaseStartTrigger trigger = caseStartTriggers.get(0);

		assertEquals("wrong CaseStart", "StartCondition", trigger.getQueryExecutionPlan());
		List<CaseStartTriggerConsequence> consequence = trigger.getTriggerConsequences();

		assertEquals("wrong CaseStartTriggerConsequence amount", 2, consequence.size());

		int dcPos = consequence.get(0).getDataObjectState().getDataClass().getName().equals("dc2") ? 0 : 1;

		AtomicDataStateCondition objectStateCondition1 = consequence.get(dcPos).getDataObjectState();

		DataClass dc = (DataClass) dataClasses.get(1);
		assertTrue("wrong DataClass mapping", objectStateCondition1.getDataClass().equals(dc));

		ObjectLifecycleState state = dc.getObjectLifecycle().getObjectLifecycleStates().get(0);
		assertTrue("wrong State mapping", objectStateCondition1.getObjectLifecycleState().equals(state));

		int dataclassPos = dcPos == 0 ? 1 : 0;

		DataAttribute attr = dataClasses.get(0).getDataAttributes().get(0);
		String jsonPathString = consequence.get(dataclassPos).getDataAttributeToJsonPath().get(attr);
		assertNotNull("wrong Attribute mapping", jsonPathString);
	}
}
