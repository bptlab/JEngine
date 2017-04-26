package de.hpi.bpt.chimera.jcomparser.json;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.verify;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import de.hpi.bpt.chimera.AbstractDatabaseDependentTest;
import de.hpi.bpt.chimera.database.data.DbDataClass;
import de.hpi.bpt.chimera.jcomparser.jaxb.DataNode;

/**
 *
 */
public class DataClassTest  extends AbstractDatabaseDependentTest {
    private String rootDataClass;
    private String normalDataClass;

    @Before
    public void setupRootDataClass(){
        rootDataClass = new JSONObject()
                .put("name", "Reise")
                .put("_id", "801101005L")
                .put("attributes", new JSONArray()
                        .put(new JSONObject()
                                .put("name", "Beginn")
                                .put("datatype", "String")
                                .put("_id", "editorId3")
                        )
                        .put(new JSONObject()
                                .put("name", "Ende")
                                .put("datatype", "String")
                                .put("_id", "editorId3")
                        )
                        .put(new JSONObject()
                                .put("name", "Gesamtkosten")
                                .put("datatype", "Float")
                                .put("_id", "editorId3")
                        )
                ).toString();

    }

    @Before
    public void setupNormalDataClass(){
        normalDataClass = new JSONObject()
                .put("name", "Flug")
                .put("_id", "679826034L")
                .put("attributes", new JSONArray()
                        .put(new JSONObject()
                                .put("name", "Abflugsdatum")
                                .put("datatype", "String")
                                .put("_id", "editorId1")
                        )
                        .put(new JSONObject()
                                .put("name", "Ankunftsdatum")
                                .put("datatype", "String")
                                .put("_id", "editorId2")
                        )
                        .put(new JSONObject()
                                .put("name", "StartFlughafen")
                                .put("datatype", "String")
                                .put("_id", "editorId3")
                        )
                        .put(new JSONObject()
                                .put("name", "EndFlughafen")
                                .put("datatype", "String")
                                .put("_id", "editorId4")
                        )
                ).toString();
    }

    @Test
    public void testRootDataClass(){
        DataClass dClass = new DataClass(rootDataClass);
        assertEquals("Name has not been set correctly", "Reise", dClass.getName());
        assertEquals("Attributes have not been set correctly", 3, dClass.getAttributes().size());
        String[] attribute = {"Beginn","Ende","Gesamtkosten"};
        for(int i = 0; i < dClass.getAttributes().size(); i++){
            assertEquals("Attribute" + i + "has not been set correctly", attribute[i], dClass.getAttributes().get(i).getName());
        }
    }

    @Test
    public void testNormalDataClass(){
        DataClass dClass = new DataClass(normalDataClass);
        assertEquals("Name has not been set correctly", "Flug", dClass.getName());
        assertEquals("Attributes have not been set correctly", 4, dClass.getAttributes().size());
        String[] attribute = {"Abflugsdatum","Ankunftsdatum","StartFlughafen", "EndFlughafen"};
        for(int i = 0; i < dClass.getAttributes().size(); i++){
            assertEquals("Attribute" + i + "has not been set correctly", attribute[i], dClass.getAttributes().get(i).getName());
        }
    }

    @Test
    public void testSaving() {
        int scenarioId = 1;

        DataClass dataClass = new DataClass(normalDataClass);
        DataNode first = EasyMock.createMock(DataNode.class);
        DataNode second = EasyMock.createMock(DataNode.class);

        first.save(dataClass, scenarioId);
        expectLastCall();
        second.save(dataClass, scenarioId);
        expectLastCall();
        replay(first);
        replay(second);

        dataClass.addDataNode(first);
        dataClass.addDataNode(second);
        int classId = dataClass.save(scenarioId);

        DbDataClass dbDataClass = new DbDataClass();
        List<Integer> dataAttributes = dbDataClass.getDataAttributes(classId);
        assertEquals(Arrays.asList(1, 2, 3, 4), dataAttributes);

        // assert that the datanodes are saved
        verify(first);
        verify(second);
    }
}
