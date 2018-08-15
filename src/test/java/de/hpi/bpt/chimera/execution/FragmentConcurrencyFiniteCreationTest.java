package de.hpi.bpt.chimera.execution;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import de.hpi.bpt.chimera.CaseExecutionerTestHelper;
import de.hpi.bpt.chimera.CaseModelTestHelper;
import de.hpi.bpt.chimera.execution.controlnodes.event.AbstractEventInstance;
import de.hpi.bpt.chimera.execution.controlnodes.event.behavior.MessageReceiveEventBehavior;
import de.hpi.bpt.chimera.model.CaseModel;
import de.hpi.bpt.chimera.persistencemanager.CaseModelManager;

public class FragmentConcurrencyFiniteCreationTest extends Unicorn {
	private final String filepath = "src/test/resources/execution/FragmentConcurrencyCreationTest.json";

	private CaseExecutioner caseExecutioner;

	@Before
	public void setUpTest() {
		super.setUpTest();
		String json = CaseModelTestHelper.getJsonString(filepath);
		CaseModel cm = CaseModelManager.parseCaseModel(json);
		caseExecutioner = ExecutionService.createCaseExecutioner(cm, cm.getName());
		caseExecutioner.startCase();
	}

	@Test
	public void testFiniteFragmentInstanceCreation() {
		int creations = 10;
		for (int i = 0; i < creations; i++) {
			Optional<MessageReceiveEventBehavior> receiveBehavior = caseExecutioner.getRegisteredEventBehaviors().stream().findFirst();
			if (receiveBehavior.isPresent()) {
				try {
					AbstractEventInstance eventInstance = receiveBehavior.get().getEventInstance();
					CaseExecutionerTestHelper.triggerEvent(caseExecutioner, eventInstance, getBase(), "");
					Thread.sleep(10);
				} catch (Exception e) {
					throw new IllegalArgumentException(i + " " + e.getMessage());
				}
			}
		}

		List<FragmentInstance> fragmentInstance = caseExecutioner.getCase().getFragmentInstances().values().stream()
													.filter(f -> f.getFragment().getName().equals("Fragment with Catch Event"))
													.collect(Collectors.toList());
		assertEquals("Not the expected amount of Fragment Instances were created", creations + 1, fragmentInstance.size());
	}
}
