package de.hpi.bpt.chimera.history;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Access point for logs of a specific Scenario Instance.
 */
public class HistoryService {
	/**
	 * Database ConnectionWrapper objects
	 */

	/**
	 * This method returns the DataObjectInstances log entries for a ScenarioInstance.
	 *
	 * @param scenarioInstanceId ID of the ScenarioInstance for which the
	 *                           DataObject log entries shall be returned.
	 * @return a Map with a Map of the log entries' attribute names
	 * as keys and their respective values.
	 */
	public List<LogEntry> getDataObjectEntries(int scenarioInstanceId) {
		return new DbLogEntry().getLogEntriesForScenarioInstance(scenarioInstanceId, LogEntry.LogType.DATA_OBJECT);
	}

	public Document getTracesForScenarioId(int scenarioId) throws ParserConfigurationException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element rootElement = doc.createElementNS("http://www.xes-standard.org", "log");
		doc.appendChild(rootElement);
		/*
		List<Integer> scenarioInstances = new DbScenarioInstance().getScenarioInstances(scenarioId);
		for (int instanceId : scenarioInstances) {
			Trace trace = new Trace(instanceId);
			trace.appendToLog(rootElement);
		}
		*/
		return doc;
	}

	/**
	 * This method returns the Activity log entries for a ScenarioInstance.
	 *
	 * @param scenarioInstanceId ID of the ScenarioInstance for which the
	 *                           activity log entries shall be returned.
	 * @return a Map with a Map of the log entries' attribute names
	 * as keys and their respective values.
	 */
	public List<LogEntry> getActivityInstanceEntries(int scenarioInstanceId) {
		StateTransitionLog.getStateTransitions(scenarioInstanceId);
		return new DbLogEntry().getLogEntriesForScenarioInstance(scenarioInstanceId, LogEntry.LogType.ACTIVITY);
	}

	public List<LogEntry> getEventEntries(int scenarioInstanceId) {
		return new DbLogEntry().getLogEntriesForScenarioInstance(scenarioInstanceId, LogEntry.LogType.EVENT);
	}

	/**
	 * This method returns the terminated Activity log entries for a ScenarioInstance.
	 *
	 * @param scenarioInstanceId ID of the ScenarioInstance for which the
	 *                           activity log entries shall be returned.
	 * @return a Map with a Map of the log entries' attribute names
	 * as keys and their respective values.
	 */
	public List<LogEntry> getTerminatedActivities(int scenarioInstanceId) {
		List<LogEntry> logEntries = new DbLogEntry().getLogEntriesForScenarioInstance(scenarioInstanceId, LogEntry.LogType.ACTIVITY);
		return logEntries.stream().filter(x -> "terminated".equals(x.getNewValue())).collect(Collectors.toList());
	}

	/**
	 * This method returns the DataAttributeInstance log entries for a ScenarioInstance.
	 *
	 * @param scenarioInstanceId ID of the ScenarioInstance for which the
	 *                           DataAttributeInstance log entries shall be returned.
	 * @return a.
	 */
	public List<LogEntry> getDataAttributeEntries(int scenarioInstanceId) {
		return new DbLogEntry().getLogEntriesForScenarioInstance(scenarioInstanceId, LogEntry.LogType.DATA_ATTRIBUTE);
	}
}
