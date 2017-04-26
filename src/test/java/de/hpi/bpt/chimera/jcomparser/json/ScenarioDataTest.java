package de.hpi.bpt.chimera.jcomparser.json;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.Test;

import de.hpi.bpt.chimera.AbstractDatabaseDependentTest;
import de.hpi.bpt.chimera.jcomparser.jaxb.DataNode;
import de.hpi.bpt.chimera.jcomparser.saving.Fragment;
import de.hpi.bpt.chimera.jcore.MockProvider;

/**
 *
 */
public class ScenarioDataTest extends AbstractDatabaseDependentTest {

    @Test
    public void testSave() throws Exception {
        String path = "src/test/resources/Scenarios/IOSetScenario.json";
        String jsonString = FileUtils.readFileToString(new File(path));
        ScenarioData data = new ScenarioData(jsonString);
        data.save();
    }

    @Test
    public void testAssociateDataNodesWithDataClasses() {
        ScenarioData scenarioData = new ScenarioData();
        DomainModel domainModel = createNiceMock(DomainModel.class);
        List<DataClass> dataClasses = mockDataclasses();
        expect(domainModel.getDataClasses()).andReturn(dataClasses);
        replay(domainModel);
        scenarioData.associateDataNodesWithDataClasses(
                createMockFragments(), domainModel);
        for (DataClass dataClass : dataClasses) {
            verify(dataClass);
        }
    }

    @Test
    public void testAssociateStatesWithDataClasses() throws JAXBException {
        ScenarioData scenarioData = new ScenarioData();
        List<Fragment> fragments = createMockFragments();
        Map<String, DataClass> nameToDataclass = mockNameToDataClass();
        scenarioData.associateStatesWithDataClasses(
                fragments, nameToDataclass);
        nameToDataclass.values().forEach(EasyMock::verify);
    }

    private Map<String, DataClass> mockNameToDataClass() {
        // TODO use method for get dataclasses
        Map<String, DataClass> nameToDataClass = new HashMap<>();
        DataClass customer = createNiceMock(DataClass.class);
        @SuppressWarnings("unchecked")
        List<String> customerStates = EasyMock.createNiceMock(ArrayList.class);
        expect(customerStates.add("received")).andReturn(true);
        expect(customerStates.add("reviewed")).andReturn(true);
        expect(customerStates.contains("received")).andReturn(true);
        expect(customerStates.contains("reviewed")).andReturn(true);
        replay(customerStates);
        expect(customer.getStates()).andReturn(customerStates).anyTimes();
        replay(customer);
        nameToDataClass.put("Customer", customer);

        DataClass contract = createNiceMock(DataClass.class);
        @SuppressWarnings("unchecked")
        List<String> contractStates = EasyMock.createNiceMock(ArrayList.class);
        expect(contractStates.add("initial")).andReturn(true);
        expect(contractStates.contains("initial")).andReturn(true);
        replay(contractStates);
        expect(contract.getStates()).andReturn(contractStates);
        replay(contract);
        nameToDataClass.put("Contract", contract);
        return nameToDataClass;
    }

    private List<DataClass> mockDataclasses() {
        List<DataClass> mockedDataclasses = new ArrayList<>();
        DataClass customer = createNiceMock(DataClass.class);
        customer.addDataNode(EasyMock.anyObject(DataNode.class));
        expectLastCall().times(2);
        expect(customer.getName()).andReturn("Customer");
        replay(customer);
        mockedDataclasses.add(customer);

        DataClass contract = createNiceMock(DataClass.class);
        expect(contract.getName()).andReturn("Contract");
        contract.addDataNode(EasyMock.anyObject(DataNode.class));
        expectLastCall().times(1);

        replay(contract);
        mockedDataclasses.add(contract);
        return mockedDataclasses;
    }

    private List<Fragment> createMockFragments() {
        List<DataNode> dataNodes = MockProvider.mockDataNodes(
                Arrays.asList("Customer", "Customer"), Arrays.asList("received", "reviewed"));
        List<DataNode> dataNodes2 = MockProvider.mockDataNodes(
                Collections.singletonList("Contract"), Collections.singletonList("initial"));
        Fragment fragment = EasyMock.createMock(Fragment.class);
        Fragment fragment2 = EasyMock.createMock(Fragment.class);
        expect(fragment.getDataNodes()).andReturn(dataNodes);
        expect(fragment2.getDataNodes()).andReturn(dataNodes2);
        replay(fragment);
        replay(fragment2);
        return Arrays.asList(fragment, fragment2);
    }
}