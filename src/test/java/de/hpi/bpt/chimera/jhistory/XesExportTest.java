package de.hpi.bpt.chimera.jhistory;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hpi.bpt.chimera.AbstractTest;
import de.hpi.bpt.chimera.ScenarioTestHelper;
import de.hpi.bpt.chimera.jcore.ScenarioInstance;
import de.hpi.bpt.chimera.jcore.rest.RestInterface;
import de.hpi.bpt.chimera.jhistory.rest.HistoryRestService;
import de.hpi.bpt.chimera.util.XmlUtil;

/**
 *
 */
public class XesExportTest extends AbstractTest {
    /**
     * The base url of the jcore rest interface.
     * Allows us to send requests to the {@link RestInterface}.
     */
    private WebTarget base;

    @Override
    protected Application configure() {
        return new ResourceConfig(HistoryRestService.class);
    }

    @Before
    public void setUpBase() {
        base = target("history/v2/");
    }


    @Test
    public void testXmlMarshalling() throws IOException, TransformerException {
        // The simple scenario contains only one activity do something
        String path = "src/test/resources/history/simpleScenario.json";
        ScenarioInstance instance = ScenarioTestHelper.createScenarioInstance(path);
        ScenarioTestHelper.beginActivityByName("Do something", instance);
        ScenarioTestHelper.terminateActivityByName("Do something", instance);

        Response response = base.path(String.format("scenario/%d/export",
                instance.getScenarioId())).request().get();
        assertEquals(200, response.getStatus());
        Document doc = XmlUtil.retrieveFromString(response.readEntity(String.class));
        Node rootElement = doc.getChildNodes().item(0);
        NodeList traces = rootElement.getChildNodes();
        assertEquals(1, traces.getLength());
        Node trace = traces.item(0);
        NodeList logEntries = trace.getChildNodes();
        // There should be 4 history entries: init, running, terminated and init again because the
        // fragment is restarted. Since we only log completed activities, there should be one entry left.
        System.out.println(XmlUtil.convertToString(doc));
        assertEquals(1, logEntries.getLength());

    }


}
