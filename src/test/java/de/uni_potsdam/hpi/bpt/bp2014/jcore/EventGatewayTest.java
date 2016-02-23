package de.uni_potsdam.hpi.bpt.bp2014.jcore;

import de.uni_potsdam.hpi.bpt.bp2014.AbstractDatabaseDependentTest;
import de.uni_potsdam.hpi.bpt.bp2014.events.EventTestHelper;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class EventGatewayTest {
    @After
    public void teardown() throws IOException, SQLException {
        AbstractDatabaseDependentTest.resetDatabase();
    }

    @Test
    public void testBoundaryDisablementWhenTerminatingActivity() {
        String path = "src/test/resources/EventScenarios/EventGatewayScenario.json";
        try {
            ScenarioInstance scenarioInstance = EventTestHelper.createScenarioInstance(path);
            List<String> registeredEvents = scenarioInstance.getRegisteredEventKeys();
        } catch (IOException e) {
            assert(false);
            e.printStackTrace();
        }
    }

    // Test whether all outgoing events are initialized from event based gateway
    @Test
    public void testEventEnablement() {
        String path = "src/test/resources/EventScenarios/EventGatewayScenario.json";
        try {
            ScenarioInstance scenarioInstance = EventTestHelper.createScenarioInstance(path);
            List<String> registeredEvents = scenarioInstance.getRegisteredEventKeys();
            assertEquals(3, registeredEvents.size());
        } catch (IOException e) {
            assert(false);
            e.printStackTrace();
        }

    }
}
