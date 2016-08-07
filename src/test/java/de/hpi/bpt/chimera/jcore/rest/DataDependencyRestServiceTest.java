package de.hpi.bpt.chimera.jcore.rest;

import de.hpi.bpt.chimera.AbstractDatabaseDependentTest;
import de.hpi.bpt.chimera.AbstractTest;
import de.hpi.bpt.chimera.jcore.rest.filters.AuthorizationRequestFilter;
import net.javacrumbs.jsonunit.core.Option;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.junit.Assert.*;

/**
 *
 */
public class DataDependencyRestServiceTest extends AbstractTest {

    private WebTarget base;

    static {
        TEST_SQL_SEED_FILE = "src/test/resources/JEngineV2RESTTest_new.sql";
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(DataDependencyRestService.class);
        config.register(AuthorizationRequestFilter.class);
        return config;
    }

    @Before
    public void setUpBase() {
        base = target("interface/v2");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        AbstractDatabaseDependentTest.resetDatabase();
    }

    @Test
    public void testGetInputDataObjectsAndAttributes(){
        Response response = base.path("scenario/135/instance/808/activityinstance/4518/input").request().get();
        assertEquals("The response code of getInputDataObjects was not 200", 200, response.getStatus());
        assertEquals("GetInputDataObjects does not return a JSON", MediaType.APPLICATION_JSON,
                response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                "{\"Reiseplan\":[\"init\"]}",
                jsonEquals(response.readEntity(String.class)).when(Option.IGNORING_ARRAY_ORDER).when(Option.IGNORING_VALUES));
    }

    @Test
    public void testGetInputForInvalidScenario() {
        Response response = base.path("scenario/9987/instance/1234/activityinstance/1/input")
                .request().get();
        assertEquals("The Response code of getInputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getInputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no scenario with id 9987\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testNotFoundInputInvalidActivityId() {
        Response response = base.path("scenario/1/instance/72/activityinstance/9999/input")
                .request().get();
        assertEquals("The Response code of getInputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getInputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no activity instance with id 9999\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testGetOutputForActivityWithoutOutput() {
        Response response = base.path("scenario/118/instance/704/activityinstance/3749/output")
                .request().get();
        assertEquals("The Response code of getOutputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getOutputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String responseJson = response.readEntity(String.class);
        assertThat("The returned JSON does not contain the expected content",
                responseJson, jsonEquals(
                        "{\"error\":\"There is no output set for activity instance 3749\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testGetOutputDataObjects(){
        Response response = base.path("scenario/135/instance/808/activityinstance/4518/output").request().get();
        assertEquals("The response code of getOutputDataObjects was not 200", 200, response.getStatus());
        assertEquals("GetOutputDataObjects does not return a JSON", MediaType.APPLICATION_JSON,
                response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                "[{\"id\":140,\"linkDataObject\":\"http://localhost:9998/interface/v2/scenario/135/instance/808/outputset/140\"}]",
                jsonEquals(response.readEntity(String.class)).when(Option.IGNORING_ARRAY_ORDER).when(Option.COMPARING_ONLY_STRUCTURE));
    }

    @Test
    public void testNotFoundInvalidActivityId() {
        Response response = base.path("scenario/1/instance/72/activityinstance/9999/output")
                .request().get();
        assertEquals("The Response code of getOutputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getOutputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no activity instance with id 9999\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testNotFoundForInvalidScenarioId() {
        Response response = base.path("scenario/0/instance/0/activityinstance/1/output")
                .request().get();
        assertEquals("The Response code of getOutputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getOutputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no scenario with id 0\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testGetInputForActivityWithoutInput() {
        Response response = base.path("scenario/135/instance/808/activityinstance/4517/input")
                .request().get();
        assertEquals("The Response code of getInputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getInputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no input set for activity instance 4517\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testGetInputSetDataAttributes() {
        Response response = base.path("scenario/135/instance/808/inputset/139")
                .request().get();
        assertEquals("The Response code of getInputDataAttributes was not 200",
                200, response.getStatus());
        assertEquals("getAttributes does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("[{\"label\":\"Reiseplan\",\"id\":675,\"state\":\"init\",\"attributeConfiguration\":[{\"id\":1,\"name\":\"Preis\",\"type\":\"String\",\"value\":\"250\"}]}]")
                        .when(Option.IGNORING_ARRAY_ORDER).when(Option.IGNORING_EXTRA_FIELDS));
    }
    @Test
    public void testGetOutputSetDataAttributes() {
        Response response = base.path("scenario/135/instance/808/outputset/140")
                .request().get();
        assertEquals("The Response code of getOutputDataAttributes was not 200",
                200, response.getStatus());
        assertEquals("getOutputDataAttributes does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String responseJson = response.readEntity(String.class);
        assertThat("The returned JSON does not contain the expected content",
                responseJson,
                jsonEquals("[{\"label\":\"Reiseplan\",\"id\":675,\"state\":\"init\",\"attributeConfiguration\":[{\"id\":1,\"name\":\"Preis\",\"type\":\"String\",\"value\":\"250\"}]}]")
                        .when(Option.IGNORING_ARRAY_ORDER).when(Option.IGNORING_EXTRA_FIELDS));
    }
    @Test
    public void testNotFoundWithInvalidScenarioId() {
        Response response = base.path("scenario/9987/instance/1234/outputset/140")
                .request().get();
        assertEquals("The Response code of getOutputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getOutputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no scenario with id 9987\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testNotFoundInvalidOutputSetId() {
        Response response = base.path("scenario/135/instance/808/outputset/1400")
                .request().get();
        assertEquals("The Response code of getOutputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getOutputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no such outputSet instance.\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testNotFoundInvalidScenarioId() {
        Response response = base.path("scenario/9987/instance/1234/inputset/140")
                .request().get();
        assertEquals("The Response code of getInputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getInputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no scenario with id 9987\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testNotFoundInvalidInputSetId() {
        Response response = base.path("scenario/135/instance/808/inputset/1400")
                .request().get();
        assertEquals("The Response code of getInputDataObjects was not 404",
                404, response.getStatus());
        assertEquals("getInputDataObjects does not return a JSON",
                MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertThat("The returned JSON does not contain the expected content",
                response.readEntity(String.class),
                jsonEquals("{\"error\":\"There is no such inputSet instance.\"}")
                        .when(Option.IGNORING_ARRAY_ORDER));
    }
}