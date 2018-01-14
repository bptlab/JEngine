package de.hpi.bpt.chimera.rest;

import de.hpi.bpt.chimera.execution.CaseExecutioner;
import de.hpi.bpt.chimera.execution.DataAttributeInstance;
import de.hpi.bpt.chimera.execution.DataManager;
import de.hpi.bpt.chimera.execution.DataObject;
import de.hpi.bpt.chimera.execution.State;
import de.hpi.bpt.chimera.execution.activity.AbstractActivityInstance;
import de.hpi.bpt.chimera.model.datamodel.DataClass;
import de.hpi.bpt.chimera.model.datamodel.ObjectLifecycleState;
import de.hpi.bpt.chimera.rest.beans.activity.ActivityJaxBean;
import de.hpi.bpt.chimera.rest.beans.datamodel.DataObjectJaxBean;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements the REST interface for activities.
 */
@Path("interface/v2")
public class ActivityRestService extends AbstractRestService {
	private final static Logger log = Logger.getLogger(ActivityRestService.class);
	/**
	 * Returns a JSON-Object containing information about all activity
	 * instances of a specified scenario instance.
	 * The JSON-Object will group the activities regarding their state.
	 * If the scenario instance does not exist, the response code will
	 * specify the error which occurred.
	 *
	 * @param uriInfo      The context object. It provides information
	 *                     the server context.
	 * @param scenarioID   The id of the scenario
	 * @param instanceID   The id of the instance.
	 * @param filterString Defines a search strings. Only activities
	 *                     with a label containing this String will be
	 *                     shown.
	 * @param state        The state of the instance.
	 * @return A Response with the status and content of the request.
	 * A 200 (OK) implies that the instance was found and the
	 * result contains the JSON-Object.
	 * If only the scenario ID is incorrect a 301 (REDIRECT)
	 * will point to the correct URL.
	 * If the instance ID is incorrect a 404 (NOT_FOUND) will
	 * be returned.
	 */
	@GET
	@Path("scenario/{scenarioId}/instance/{instanceId}/activity")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActivitiesOfInstance(@Context UriInfo uriInfo, @PathParam("scenarioId") String cmId, @PathParam("instanceId") String caseId, @QueryParam("filter") String filterString, @QueryParam("state") String stateName) {
		try {
			CaseExecutioner caseExecutioner = de.hpi.bpt.chimera.execution.ExecutionService.getCaseExecutioner(cmId, caseId);
			// common
			if (StringUtils.isEmpty(filterString)) {
				if (StringUtils.isEmpty(stateName)) {
					// for log
					return getAllActivitiesOfInstance(caseExecutioner, uriInfo);
				}
				// for execution
				return getAllActivitiesOfInstanceWithState(caseExecutioner, stateName, uriInfo);
			}
			// uncommon
			if (StringUtils.isEmpty(stateName)) {
				return getAllActivitiesOfInstanceWithFilter(caseExecutioner, filterString, uriInfo);
			}
			return getAllActivitiesWithFilterAndState(caseExecutioner, filterString, stateName, uriInfo);
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(buildException(e.getMessage())).build();
		}
	}

	/**
	 * Returns a Response Object for all activities with the instance Id. We
	 * assume that the instanceId is correct. The Response will be a 200 with
	 * json content. The Content will be a json object with information about
	 * each activity.
	 *
	 * @param instanceID
	 *            the instance id of the scenario instance.
	 * @return The Response Object, with 200 and JSON Content.
	 */
	private Response getAllActivitiesOfInstance(CaseExecutioner caseExecutioner, UriInfo uriInfo) {
		return getAllActivitiesOfInstanceWithFilter(caseExecutioner, "", uriInfo);
	}

	/**
	 * Returns a Response Object. The Response Object will be a 200 with JSON
	 * content. The Content will be a JSON Object, containing information about
	 * activities. The Label of the activities must correspond to the filter
	 * String and be part of the scenario instance specified by the instanceId.
	 * 
	 * @param caseExecutioner
	 * @param filterString
	 * @param uriInfo
	 * @return The created Response object with a 200 and a JSON.
	 */
	private Response getAllActivitiesOfInstanceWithFilter(CaseExecutioner caseExecutioner, String filterString, UriInfo uriInfo) {
		Collection<AbstractActivityInstance> activityInstances = new ArrayList<>();

		activityInstances.addAll(caseExecutioner.getActivitiesWithState(State.READY));
		activityInstances.addAll(caseExecutioner.getActivitiesWithState(State.RUNNING));
		activityInstances.addAll(caseExecutioner.getActivitiesWithState(State.TERMINATED));
		activityInstances.addAll(caseExecutioner.getActivitiesWithState(State.DATAFLOW_ENABLED));
		activityInstances.addAll(caseExecutioner.getActivitiesWithState(State.CONTROLFLOW_ENABLED));

		if (!filterString.isEmpty()) {
			activityInstances = activityInstances.stream().filter(instance -> instance.getControlNode().getName().contains(filterString)).collect(Collectors.toList());
		}

		JSONArray result = new JSONArray();
		for (AbstractActivityInstance activityInstance : activityInstances) {
			result.put(new JSONObject(new ActivityJaxBean(activityInstance)));
		}

		return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * This method creates a Response object for all specified activities. The
	 * activities are specified by an scenario instance and a state. In addition
	 * UriInfo object is needed in order to create the links to the activity
	 * instances.
	 * 
	 * @param caseExecutioner
	 * @param stateName
	 * @param uriInfo
	 * @return
	 */
	private Response getAllActivitiesOfInstanceWithState(CaseExecutioner caseExecutioner, String stateName, UriInfo uriInfo) {
		return getAllActivitiesWithFilterAndState(caseExecutioner, "", stateName, uriInfo);
	}

	/**
	 * Returns a Response object. The Object will be either a 200 with the
	 * activities in an JSON-Object or an 400 with an error message if the state
	 * is invalid.
	 * 
	 * @param caseExecutioner
	 * @param filterString
	 * @param stateName
	 * @param uriInfo
	 * @return
	 */
	private Response getAllActivitiesWithFilterAndState(CaseExecutioner caseExecutioner, String filterString, String stateName, UriInfo uriInfo) {
		try {
			State state = State.fromString(stateName);
			Collection<AbstractActivityInstance> activityInstances = caseExecutioner.getActivitiesWithState(state);
			if (!filterString.isEmpty()) {
				activityInstances = activityInstances.stream().filter(instance -> instance.getControlNode().getName().contains(filterString)).collect(Collectors.toList());
			}

			JSONArray result = new JSONArray();
			for (AbstractActivityInstance activityInstance : activityInstances) {
				result.put(new JSONObject(new ActivityJaxBean(activityInstance)));
			}

			return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(buildException(e.getMessage())).build();
		}
	}

	/**
	 * This method is used to get all the information for an activity.
	 * This means the label, id and a link for the input-/outputSets.
	 *
	 * @param uriInfo            A UriInfo object, which holds the server context.
	 * @param scenarioId         The databaseID of a scenario.
	 * @param scenarioInstanceId The databaseID of a scenarioInstance.
	 * @param activityInstanceId The databaseID of an activityInstance.
	 * @return a response Object with the status code:
	 * 200 if everything was correct and holds the information about the activityInstance.
	 * A 404 Not Found is returned if the scenario/scenarioInstance/activityInstanceID is wrong.
	 */
	@GET
	@Path("scenario/{scenarioId}/instance/{instanceId}/activityinstance/{activityInstanceId}")
	public Response getActivity(@Context UriInfo uriInfo, @PathParam("scenarioId") String cmId, @PathParam("instanceId") String caseId, @PathParam("activityInstanceId") String activityInstanceId) {
		try {
			CaseExecutioner caseExecutioner = de.hpi.bpt.chimera.execution.ExecutionService.getCaseExecutioner(cmId, caseId);
			AbstractActivityInstance activityInstance = caseExecutioner.getActivityInstance(activityInstanceId);
			ActivityJaxBean activity = new ActivityJaxBean(activityInstance);
			return Response.ok(activity, MediaType.APPLICATION_JSON).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(buildException(e.getMessage())).build();
		}
	}


	/**
	 * This method updates the data attributes of a specific activity
	 * defined via its activityID.
	 *
	 * @param scenarioId         The id of a scenario model.
	 * @param scenarioInstanceId the id of an scenario instance.
	 * @param activityInstanceId the control node instance id of the activity.
	 * @param input              data input.
	 * @return Status code with regard to its success / failure
	 */
	@PUT
	@Path("scenario/{scenarioId}/instance/{instanceId}/activityinstance/{activityInstanceId}")
	public Response setDataAttribute(@PathParam("scenarioId") String cmId, @PathParam("instanceId") String caseId, @PathParam("activityInstanceId") String activityInstanceId, @DefaultValue("") String post) {
		log.info("REST: setting DataAttributeInstances");
		try {
			CaseExecutioner caseExecutioner = de.hpi.bpt.chimera.execution.ExecutionService.getCaseExecutioner(cmId, caseId);
			AbstractActivityInstance activityInstance = caseExecutioner.getActivityInstance(activityInstanceId);

			Map<String, Map<String, Object>> dataAttributeValues = parseDataAttribueValues(post);

			DataManager dataManager = caseExecutioner.getDataManager();
			dataManager.setDataAttributeValues(dataAttributeValues);

			caseExecutioner.terminateActivityInstance(activityInstance);

			return Response.status(201).build();
		} catch (IllegalArgumentException e) {
			log.error("REST setDataAttribute() Error", e);
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(buildException(e.getMessage())).build();
		}
	}

	/**
	 * Parse the json post in a map of DataObject-Id to the a map of
	 * DataAttributeInstance-Id to new DataAttribute-value.
	 * 
	 * @param post
	 *            - JSON
	 * @return map from DataObjectId to DataAttributeInstanceId to new
	 *         DataAttributeInstance value
	 */
	private Map<String, Map<String, Object>> parseDataAttribueValues(String post) {
		Map<String, Map<String, Object>> dataAttributeValues = new HashMap<>();
		JSONObject dataObjectJson = new JSONObject(post);
		for (Object dataObject : dataObjectJson.keySet()) {
			if (dataObject.getClass() != String.class)
				continue;
			String dataObjectId = (String) dataObject;

			JSONObject attributeJson = dataObjectJson.getJSONObject(dataObjectId);
			Map<String, Object> dataAttributeValue = new HashMap<>();
			for (Object attribute : attributeJson.keySet()) {
				String attributeId = (String) attribute;
				dataAttributeValue.put(attributeId, attributeJson.get(attributeId));
			}
			dataAttributeValues.put(dataObjectId, dataAttributeValue);
		}
		return dataAttributeValues;
	}
	/**
	 * @param scenarioId         The id of the scenario.
	 * @param scenarioInstanceId The id of the scenario instance.
	 * @param activityInstanceId The id of the activity instance.
	 * @return A list with working items for the activity instance.
	 */
	@GET
	@Path("scenario/{scenarioId}/instance/{instanceId}/activityinstance/{activityInstanceId}/workingItems")
	public Response getWorkingItems(@PathParam("scenarioId") String cmId, @PathParam("instanceId") String caseId, @PathParam("activityInstanceId") String activityInstanceId) {
		try {
			CaseExecutioner caseExecutioner = de.hpi.bpt.chimera.execution.ExecutionService.getCaseExecutioner(cmId, caseId);
			AbstractActivityInstance activityInstance = caseExecutioner.getActivityInstance(activityInstanceId);

			List<DataObject> selectedInstances = activityInstance.getSelectedDataObjects();

			JSONArray result = new JSONArray();
			for (DataObject dataObject : selectedInstances) {
				result.put(new JSONObject(new DataObjectJaxBean(dataObject)));
			}
			return Response.status(Response.Status.ACCEPTED).type(MediaType.APPLICATION_JSON).entity(result.toString()).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(buildException(e.getMessage())).build();
		}
	}

	/**
	 * Changes the state of an activityInstance from enabled to running.
	 *
	 * @param scenarioId         The id of a scenario model.
	 * @param scenarioInstanceId the id of an scenario instance.
	 * @param activityInstanceId the id of the activity instance.
	 * @param postBody           Json Object containing the data objects on which the activity operates
	 * @return a message regarding the success of the operation
	 * A 202 (ACCEPTED) means that the POST was successful.
	 * A 400 (BAD_REQUEST) if the transition was not allowed.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("scenario/{scenarioId}/instance/{instanceId}/activityinstance/{activityInstanceId}/begin")
	public Response beginActivity(@PathParam("scenarioId") String cmId, @PathParam("instanceId") String caseId, @PathParam("activityInstanceId") String activityInstanceId, @DefaultValue("") String post) {
		try {
			CaseExecutioner caseExecutioner = de.hpi.bpt.chimera.execution.ExecutionService.getCaseExecutioner(cmId, caseId);

			AbstractActivityInstance activityInstance = caseExecutioner.getActivityInstance(activityInstanceId);

			List<String> selectedDataObjectIds = parseDataObjectIds(post);
			List<DataObject> selectedDataObjects = caseExecutioner.getDataManager().getDataObjectsById(selectedDataObjectIds);

			caseExecutioner.beginActivityInstance(activityInstance, selectedDataObjects);
			return Response.status(Response.Status.ACCEPTED).type(MediaType.APPLICATION_JSON).entity("{\"message\":\"activity begun.\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(buildException(e.getMessage())).build();
		}
	}

	/**
	 * Parse a List of DataObjectIds of the json post with the keyword
	 * {@code dataobjects}.
	 * 
	 * @param post
	 *            - JSONString
	 * @return List of Stings
	 */
	private List<String> parseDataObjectIds(String post) {
		List<String> dataObjectIds = new ArrayList<>();
		JSONObject postJson = new JSONObject(post);
		if (postJson.has("dataobjects")) {
			JSONArray dataObjectsJson = postJson.getJSONArray("dataobjects");
			for (int i = 0; i < dataObjectsJson.length(); i++) {
				String dataObjectId = dataObjectsJson.getString(i);
				dataObjectIds.add(dataObjectId);
			}
		}
		return dataObjectIds;
	}

	/**
	 * Changes the state of of an activity instance from running to terminated.
	 *
	 * @param scenarioId         Id of the scenario model.
	 * @param scenarioInstanceId Id of the model instance.
	 * @param activityInstanceId Id of the activity instance to terminate
	 * @param postBody           Json Body containing a map from name of data object to state
	 *                           specifying the resulting states of the data objects the activity works on
	 * @return 202 (ACCEPTED) means that the activity was terminated successfully
	 * 400 (BAD_REQUEST) Termination of the activity failed. Possible reasons are:
	 * 1) The activity was not running
	 * 2) The wanted state does not comply to the OLC
	 * 3) The Body specifies not a resulting state for each data object.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("scenario/{scenarioId}/instance/{instanceId}/activityinstance/{activityInstanceId}/terminate")
	public Response terminateActivity(@PathParam("scenarioId") String cmId, @PathParam("instanceId") String caseId, @PathParam("activityInstanceId") String activityInstanceId, @DefaultValue("") String post) {
		try {
			CaseExecutioner caseExecutioner = de.hpi.bpt.chimera.execution.ExecutionService.getCaseExecutioner(cmId, caseId);

			AbstractActivityInstance activityInstance = caseExecutioner.getActivityInstance(activityInstanceId);

			Map<String, String> dataClassToStateTransitionStrings = parseDataClassToStateTransitionStrings(post);
			Map<DataClass, ObjectLifecycleState> dataClassToStateTransitions = caseExecutioner.getDataManager().resolveDataClassToStateTransition(dataClassToStateTransitionStrings);
			List<DataObject> usedDataObjects = caseExecutioner.handleActivityOutputTransitions(activityInstance, dataClassToStateTransitions);

			JSONArray result = parseJsonForUsedDataObjects(usedDataObjects);
			
			return Response.status(Response.Status.ACCEPTED).type(MediaType.APPLICATION_JSON).entity(result.toString()).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(buildException(e.getMessage())).build();
		}
	}
	
	/**
	 * Parse the json body in map from dataclass of the DataObject to the new
	 * ObjectLifecycleState.
	 * 
	 * @param post
	 * @return map from DataClassName to ObjectLifecycleName
	 */
	Map<String, String> parseDataClassToStateTransitionStrings(String post) {
		Map<String, String> dataClassToStateTransitionStrings = new HashMap<>();
		
		JSONObject transitions = new JSONObject(post);
		
		for (Object rawDataClass : transitions.keySet()) {
			String dataClassName = (String) rawDataClass;
			String olcStateName = (String) transitions.get(dataClassName);
			dataClassToStateTransitionStrings.put(dataClassName, olcStateName);
		}

		return dataClassToStateTransitionStrings;
	}

	/**
	 * Parse a json object for the used DataObjects during terminating an
	 * {@link AbstractActivityInstance}. Therefore get the ids and DataClasses
	 * by the DataObjects and the name and ids of their DataAttributeInstances
	 * to exchange the name in the running frontend.
	 * 
	 * @param usedDataObjects
	 * @return JSONArray
	 */
	JSONArray parseJsonForUsedDataObjects(List<DataObject> usedDataObjects) {
		JSONArray result = new JSONArray();
		for (DataObject dataObject : usedDataObjects) {
			JSONObject dataObjectInfo = new JSONObject();
			dataObjectInfo.put("id", dataObject.getId());
			dataObjectInfo.put("dataclass", dataObject.getDataClass().getName());
			JSONArray attributeInstanceInfo = new JSONArray();
			for (DataAttributeInstance dataAttributeInstance : dataObject.getDataAttributeInstanceIdToInstance().values()) {
				JSONObject attributeInstance = new JSONObject();
				attributeInstance.put("name", dataAttributeInstance.getDataAttribute().getName());
				attributeInstance.put("id", dataAttributeInstance.getId());
				attributeInstanceInfo.put(attributeInstance);
			}
			dataObjectInfo.put("attributeInstances", attributeInstanceInfo);
			result.put(dataObjectInfo);
		}
		return result;
	}
}
