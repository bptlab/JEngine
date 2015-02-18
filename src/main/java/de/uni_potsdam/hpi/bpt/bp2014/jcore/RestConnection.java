package de.uni_potsdam.hpi.bpt.bp2014.jcore;

import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * ********************************************************************************
 *
 * _________ _______  _        _______ _________ _        _______
 * \__    _/(  ____ \( (    /|(  ____ \\__   __/( (    /|(  ____ \
 * )  (  | (    \/|  \  ( || (    \/   ) (   |  \  ( || (    \/
 * |  |  | (__    |   \ | || |         | |   |   \ | || (__
 * |  |  |  __)   | (\ \) || | ____    | |   | (\ \) ||  __)
 * |  |  | (      | | \   || | \_  )   | |   | | \   || (
 * |\_)  )  | (____/\| )  \  || (___) |___) (___| )  \  || (____/\
 * (____/   (_______/|/    )_)(_______)\_______/|/    )_)(_______/
 *
 * ******************************************************************
 *
 * Copyright © All Rights Reserved 2014 - 2015
 *
 * Please be aware of the License. You may found it in the root directory.
 *
 * **********************************************************************************
 */


@Path("interface/v1/en/") //defining also version and language
public class RestConnection {
    private ExecutionService executionService = new ExecutionService();
    private HistoryService historyService = new HistoryService();

    /* ######################################################
     *
     * HTTP GET REQUESTS
     *
     * #######################################################
     */

    /**
     * GET  information about an activityID
     *
     * @param scenarioID the ID of the related scenario
     * @return returns JSON containing details for scenarios
     */

    @GET
    @Path("scenario/{scenarioID}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showDetailsForScenarios(@PathParam("scenarioID") int scenarioID) {
        //if 0 as scenarioID is provided, list all available scenarioIDs
        if (scenarioID == 0) {
            LinkedList<Integer> scenarioIDs = executionService.getAllScenarioIDs();
            //if no scenario is present -> return empty
            if (scenarioIDs.size() == 0) {
                return Response.ok(new String("{empty}"), MediaType.APPLICATION_JSON_TYPE).build();
            }
            String jsonRepresentation = JsonWrapperLinkedList(scenarioIDs);

            return Response.ok(jsonRepresentation, MediaType.APPLICATION_JSON).build();
        //otherwise display the label for the scenarioID
        } else {
            String label = executionService.getScenarioName(scenarioID);
            //if no activity with this id is present -> throw error
            if (label.equals("")) {
                return Response.serverError().entity("Error: not correct scenarioID").build();
            }
            return Response.ok(new String("{\"" + label + "\"}"), MediaType.APPLICATION_JSON).build();
        }
    }


    /**
     * GET all scenarioInstanceIDs  of a scenario
     *
     * @param scenarioID the ID of the related scenario
     * @param instanceID the ID of the related scenario instance
     * @return details for a scenario instance or a error code
     */

    @GET
    @Path("scenario/{scenarioID}/instance/{instanceID}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showScenarioInstances(@PathParam("scenarioID") int scenarioID, @PathParam("instanceID") int instanceID) {
        //if instanceID is null, display all available instances for the mentioned scenarioID
        if (instanceID == 0) {
            // if no Scenario exists
            if (!executionService.existScenario(scenarioID)) {
                return Response.serverError().entity("Error: not a correct scenario").build();
            }
            LinkedList<Integer> scenarioIDs = executionService.listAllScenarioInstancesForScenario(scenarioID);
            //if no scenario is present -> return empty
            if (scenarioIDs.size() == 0) {
                return Response.ok(new String("{empty}"), MediaType.APPLICATION_JSON_TYPE).build();
            }
            String jsonRepresentation = JsonWrapperLinkedList(scenarioIDs);

            return Response.ok(jsonRepresentation, MediaType.APPLICATION_JSON).build();

            //otherwise display details for this instanceID
            //TODO: implement returning of instance details (label, timestamp..)
        } else {
            String label = executionService.getScenarioNameForScenarioInstance(instanceID);
            //get the scenarioID for this instance
            //int scenarioID = executionService.getScenarioIDForScenarioInstance(instanceID);

            //if no scenario instance with this id is present -> throw error
            if (label.equals("")) {
                return Response.serverError().entity("Error: not correct instanceID").build();
            }
            return Response.ok(new String("{\"" + label + "\"}"), MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * GET  details for an activityID
     *
     * @param scenarioID         the ID of the related scenario
     * @param instanceID         the ID of the related scenario instance
     * @param activityinstanceID the ID of the related activity instance
     * @param status             the new status of the activity which is supposed to be updated
     * @param limit              a limit which is not used yet but defined through API specs
     * @return details for an activity
     * <p/>
     * TODO: Limit has to be implemented
     */

    @GET
    @Path("scenario/{scenarioID}/instance/{instanceID}/activityinstance/{activityinstanceID}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showDetailsForActivity(@PathParam("scenarioID") int scenarioID, @PathParam("instanceID") int instanceID, @PathParam("activityinstanceID") int activityinstanceID, @QueryParam("status") String status, @QueryParam("limit") String limit) {
        //display all open activities if ID is 0
        if (activityinstanceID == 0) {
            // if status is not set, set default value
            if (status == null) {
                status = "enabled";
            }
            // if status is enabled -> return all enabled activities
            if (status.equals("enabled")) {

                if (!executionService.openExistingScenarioInstance(new Integer(scenarioID), new Integer(instanceID))) {
                    return Response.serverError().entity("Error: not a correct scenario instance").build();
                }
                LinkedList<Integer> enabledActivitiesIDs = executionService.getEnabledActivitiesIDsForScenarioInstance(instanceID);
                HashMap<Integer, String> labels = executionService.getEnabledActivityLabelsForScenarioInstance(instanceID);
                // iff no open activities present return {empty}
                if (enabledActivitiesIDs.size() == 0) {
                    return Response.ok(new String("{empty}"), MediaType.APPLICATION_JSON_TYPE).build();
                }
                String jsonRepresentation = JsonWrapperHashMap(enabledActivitiesIDs, labels);

                return Response.ok(jsonRepresentation, MediaType.APPLICATION_JSON).build();
                // if status is terminated -> return all terminated activities
            } else if (status.equals("terminated")) {

                if (!executionService.existScenarioInstance(scenarioID, instanceID)) {
                    return Response.serverError().entity("Error: not a correct scenario instance").build();
                }
                LinkedList<Integer> terminatedActivities = historyService.getTerminatedActivitiesForScenarioInstance(instanceID);
                HashMap<Integer, String> labels = historyService.getTerminatedActivityLabelsForScenarioInstance(instanceID);
                //if no closed activities present -> return {empty}
                if (terminatedActivities.size() == 0) {
                    return Response.ok(new String("{empty}"), MediaType.APPLICATION_JSON_TYPE).build();
                }
                String jsonRepresentation = JsonWrapperHashMap(terminatedActivities, labels);

                return Response.ok(jsonRepresentation, MediaType.APPLICATION_JSON).build();
                // if status is running -> return all running activities
            } else if (status.equals("running")) { //running activities;

                if (!executionService.openExistingScenarioInstance(new Integer(scenarioID), new Integer(instanceID))) {
                    return Response.serverError().entity("Error: not a correct scenario instance").build();
                }
                LinkedList<Integer> enabledActivitiesIDs = executionService.getRunningActivitiesIDsForScenarioInstance(instanceID);
                HashMap<Integer, String> labels = executionService.getRunningActivityLabelsForScenarioInstance(instanceID);
                // if no running activities present -> return {empty}
                if (enabledActivitiesIDs.size() == 0) {
                    return Response.ok(new String("{empty}"), MediaType.APPLICATION_JSON_TYPE).build();
                }
                String jsonRepresentation = JsonWrapperHashMap(enabledActivitiesIDs, labels);

                return Response.ok(jsonRepresentation, MediaType.APPLICATION_JSON).build();

            }
            // if status is not satisfying declared (!= {enabled,terminated,running} thorugh error
            return Response.serverError().entity("Error: status not clear").build();

            // display details for this activityID
            //TODO: implement returning of timestamp and additional details
            //if activity ID is != 0 then display details for this activity
        } else {
            String label = executionService.getLabelForControlNodeID(activityinstanceID);
            //if no activity with this id present
            if (label.equals("")) {
                return Response.serverError().entity("Error: not correct Activity ID").build();
            }
            return Response.ok(new String("{\"" + label + "\"}"), MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * GET Dataobjects and States
     *
     * @param scenarioID         the ID of the related scenario
     * @param scenarioInstanceID the ID of the related scenario instance
     * @param status             the new status of the activity which is supposed to be updated
     * @return json representation of all available dataobjects
     */

    @GET
    @Path("scenario/{scenarioID}/instance/{instanceID}/dataobject/{dataobjectID}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response showDataObjects(@PathParam("scenarioID") int scenarioID, @PathParam("instanceID") int scenarioInstanceID, @PathParam("dataobjectID") int dataobjectID, @QueryParam("status") String status, @QueryParam("limit") String limit) {

        if (!executionService.openExistingScenarioInstance(new Integer(scenarioID), new Integer(scenarioInstanceID))) {
            return Response.serverError().entity("Error: there is no existing open scenario instance").build();
        }
        LinkedList<Integer> dataObjects = executionService.getAllDataObjectIDs(scenarioInstanceID);
        HashMap<Integer, String> labels = executionService.getAllDataObjectStates(scenarioInstanceID);
        //if no dataobject is available -> return {empty}
        if (dataObjects.size() == 0) {
            return Response.ok(new String("{empty}"), MediaType.APPLICATION_JSON_TYPE).build();
        }
        String jsonRepresentation = JsonWrapperHashMap(dataObjects, labels);

        return Response.ok(jsonRepresentation, MediaType.APPLICATION_JSON).build();
    }


    /* #############################################################################
     *
     * HTTP POST REQUEST
     *
     * #############################################################################
     */

    /**
     * POST to start a new instance
     *
     * @param scenarioID defines the ID of the scenario
     * @return error status or id of new instance
     */

    @POST
    @Path("scenario/{scenarioID}/")
    public int startNewScenarioInstance(@PathParam("scenarioID") int scenarioID) {
        // if scenario exists
        if (executionService.existScenario(scenarioID)) {
            //return the ID of new instanceID
            return executionService.startNewScenarioInstance(scenarioID);
            // else scenario does not exist
        } else {
            return -1;
        }
    }

    /**
     * POST to change status of an activity
     *
     * @param scenarioID         the ID of the related scenario
     * @param scenarioInstanceID the ID of the related scenario instance
     * @param activityInstanceID the ID of the related activity instance
     * @param status             the new status of the activity which is supposed to be updated
     * @return true or false
     */

    @POST
    @Path("scenario/{scenarioID}/instance/{instanceID}/activityinstance/{activityinstanceID}/")
    public Boolean doActivity(@PathParam("scenarioID") String scenarioID, @PathParam("instanceID") int scenarioInstanceID, @PathParam("activityinstanceID") int activityInstanceID, @QueryParam("status") String status) {
        Boolean result;
        executionService.openExistingScenarioInstance(new Integer(scenarioID), new Integer(scenarioInstanceID));
        // check on status, if begin -> start the activity
        if (status.equals("begin")) {
            result = executionService.beginActivity(scenarioInstanceID, activityInstanceID);
            if (result) {
                return true;
            } else {
                System.err.print("ERROR within the executionService.beginActivity during REST Call doActivity");
                return false;
            }
            // otherwise when terminate -> terminate the activity
        } else if (status.equals("terminate")) {
            result = executionService.terminateActivity(scenarioInstanceID, activityInstanceID);
            if (result) {
                return true;
            } else {
                System.err.print("ERROR within the executionService.terminateActivity during REST Call doActivity");
                return false;
            }
        }
        //return Response.serverError().entity("Error: status not clear").build();//status != {begin,begin}
        System.err.print("ERROR: no status defined " + status);
        return false;
    }


    /* ##########################################################################
     *
     * HELPER CLASSES
     *
     * ##########################################################################
     */

    /**
     * @param content contains a LinkedList
     * @return a wrapped json
     */
    // we are so cool, we dont need any libraries :/
    private String JsonWrapperLinkedList(LinkedList<Integer> content) {
        Gson gson = new Gson();
        JsonIntegerList json = new JsonIntegerList(content);
        return gson.toJson(json);
    }

    /**
     * @param content contains a LinkedList
     * @param labels  contains a String
     * @return a wrapped json
     */
    // we are so cool, we dont need any libraries :/
    private String JsonWrapperHashMap(LinkedList<Integer> content, HashMap<Integer, String> labels) {
        Gson gson = new Gson();
        JsonHashMapIntegerString json = new JsonHashMapIntegerString(content, labels);
        return gson.toJson(json);
    }

    class JsonHashMapIntegerString {
        private LinkedList<Integer> ids;
        private HashMap<Integer, String> label;

        public JsonHashMapIntegerString(LinkedList<Integer> ids, HashMap<Integer, String> labels) {
            this.ids = ids;
            this.label = labels;
        }
    }

    class JsonIntegerList {
        private LinkedList<Integer> ids;

        public JsonIntegerList(LinkedList<Integer> ids) {
            this.ids = ids;
        }
    }

    class JsonInteger {
        private Integer id;

        public JsonInteger(Integer id) {
            this.id = id;
        }
    }
}
