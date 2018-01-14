package de.hpi.bpt.chimera.execution.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.model.condition.ConditionSet;
import de.hpi.bpt.chimera.execution.CaseExecutioner;
import de.hpi.bpt.chimera.execution.exception.IllegalDataClassNameException;
import de.hpi.bpt.chimera.execution.exception.IllegalDataObjectIdException;
import de.hpi.bpt.chimera.execution.exception.IllegalDataObjectLockException;
import de.hpi.bpt.chimera.execution.exception.IllegalDataObjectUnlockException;
import de.hpi.bpt.chimera.execution.exception.IllegalObjectLifecycleStateNameException;
import de.hpi.bpt.chimera.execution.exception.IllegalObjectLifecycleStateSuccessorException;
import de.hpi.bpt.chimera.model.condition.AtomicDataStateCondition;
import de.hpi.bpt.chimera.model.datamodel.DataClass;
import de.hpi.bpt.chimera.model.datamodel.DataModel;
import de.hpi.bpt.chimera.model.datamodel.ObjectLifecycleState;

/**
 * I manage the data objects of a case.
 */
public class DataManager {
	private static final Logger log = Logger.getLogger(DataManager.class);

	private CaseExecutioner caseExecutioner;
	private DataModel dataModel;
	private Map<String, DataObject> dataObjectIdToDataObject;

	public DataManager(DataModel dataModel, CaseExecutioner caseExecutioner) {
		this.setDataModel(dataModel);
		this.setCaseExecutioner(caseExecutioner);
		this.dataObjectIdToDataObject = new HashMap<>();
	}

	public DataObject createDataObject(AtomicDataStateCondition condition) {
		DataObject dataObject = new DataObject(condition, this);
		dataObjectIdToDataObject.put(dataObject.getId(), dataObject);
		return dataObject;
	}

	/**
	 * Resolve a Map of DataClass name and ObjectLifecycle name to the
	 * corresponding DataClass and ObjectLifecycleState.
	 * 
	 * @param dataClassToStateTransitionStrings
	 * @return Map from DataClass to ObjectLifecycleState
	 */
	public Map<DataClass, ObjectLifecycleState> resolveDataClassToStateTransition(Map<String, String> dataClassToStateTransitionStrings) {
		Map<DataClass, ObjectLifecycleState> dataClassToStateTransition = new HashMap<>();
		Map<String, DataClass> dataClassNameToDataClass = dataModel.getNameToDataClass();
		for (Entry<String, String> dataClassNameToStateName : dataClassToStateTransitionStrings.entrySet()) {
			String dataClassName = dataClassNameToStateName.getKey();
			if (!dataClassNameToDataClass.containsKey(dataClassName)) {
				IllegalDataClassNameException e = new IllegalDataClassNameException(dataClassName);
				log.error(e.getMessage());
				throw e;
			}
			DataClass dataClass = dataClassNameToDataClass.get(dataClassName);
			String objectLifecycleStateName = dataClassNameToStateName.getValue();
			Map<String, ObjectLifecycleState> olcStateNameToOlcState = dataClass.getNameToObjectLifecycleState();
			if (!olcStateNameToOlcState.containsKey(objectLifecycleStateName)) {
				IllegalObjectLifecycleStateNameException e = new IllegalObjectLifecycleStateNameException(dataClass, dataClassName);
				log.error(e.getMessage());
				throw e;
			}
			ObjectLifecycleState olcState = olcStateNameToOlcState.get(objectLifecycleStateName);
			dataClassToStateTransition.put(dataClass, olcState);
		}
		return dataClassToStateTransition;
	}

	/**
	 * Handle the transitions and new creations of DataObject that shell make a
	 * transition specified in {@code dataClassToStateTransitions}. Therefore
	 * validate if the DataClasses of these DataObjects exist in
	 * {@code dataClassToStateTransitions} and if the the referred
	 * {@link ObjectLifecycle} is a valid successor of the current
	 * ObjectLifecycle of the DataObject. The ObjectLifecycleStates of the
	 * {@code dataObjectsToTransition} make the designated transition and the
	 * DataClasses that are in dataClassToStateTransitions but not a DataClass
	 * of one {@code dataObjectsToTransition} lead to a new DataObject with the
	 * referred ObjectLifecycleState.
	 * 
	 * @param workingItems
	 * @param dataClassToStateTransitions
	 * @return the List of DataObject that had a transition and were newly
	 *         created
	 */
	public List<DataObject> handleDataObjectTransitions(List<DataObject> workingItems, Map<DataClass, ObjectLifecycleState> dataClassToStateTransitions) {
		// validation
		List<DataObject> dataObjectToTransition = new ArrayList<>();
		for (DataObject dataObject : workingItems) {
			DataClass dataClass = dataObject.getDataClass();
			if (!dataClassToStateTransitions.containsKey(dataClass)) {
				continue;
			}
			dataObjectToTransition.add(dataObject);
			ObjectLifecycleState oldOlcState = dataObject.getObjectLifecycleState();
			ObjectLifecycleState newOlcState = dataClassToStateTransitions.get(dataClass);
			if (!newOlcState.isSuccessorOf(oldOlcState)) {
				IllegalObjectLifecycleStateSuccessorException e = new IllegalObjectLifecycleStateSuccessorException(dataClass, oldOlcState, newOlcState);
				log.error(e.getMessage());
				throw e;
			}
		}

		// make transitions
		List<DataClass> workingItemDataClasses = new ArrayList<>();
		for (DataObject workingItem : dataObjectToTransition) {
			ObjectLifecycleState newObjectLifecycleState = dataClassToStateTransitions.get(workingItem.getDataClass());
			workingItem.makeObjectLifecycleTransition(newObjectLifecycleState);
			workingItemDataClasses.add(workingItem.getDataClass());
		}
		
		// create new DataObjects
		List<DataClass> transitionDataClasses = new ArrayList<>(dataClassToStateTransitions.keySet());
		transitionDataClasses.removeAll(workingItemDataClasses);
		for (DataClass dataClass : transitionDataClasses) {
			ObjectLifecycleState olcState = dataClassToStateTransitions.get(dataClass);
			AtomicDataStateCondition condition = new AtomicDataStateCondition(dataClass, olcState);
			DataObject dataObject = createDataObject(condition);
			dataObjectToTransition.add(dataObject);
		}

		return dataObjectToTransition;
	}

	/**
	 * Lock those {@link DataObject}{@code s} that are referred by the
	 * dataObjectIds and returns these DataObjects. If a DataObject shall be
	 * locked that is already locked throw a SecurityException.
	 * 
	 * @param dataObjectIds
	 *            - list of {@code ids} which referring DataObjects shall be
	 *            locked
	 * @return List of locked DataObjects
	 */
	public List<DataObject> lockDataObjects(List<DataObject> dataObjects) {
		List<DataObject> lockedDataObjects = new ArrayList<>();
		for (DataObject dataObjectToLock : dataObjects) {
			if (dataObjectToLock.isLocked()) {
				unlockDataObjects(lockedDataObjects);
				IllegalDataObjectLockException e = new IllegalDataObjectLockException(dataObjectToLock);
				log.error(e.getMessage());
				throw e;
			}
			dataObjectToLock.lock();
			lockedDataObjects.add(dataObjectToLock);
		}

		return lockedDataObjects;
	}

	/**
	 * Unlock certain {@link DataObject}{@code s}.
	 * 
	 * @param dataObjects
	 *            - that shall be unlocked
	 */
	public void unlockDataObjects(List<DataObject> dataObjects) {
		List<DataObject> unlockedDataObjects = new ArrayList<>();

		for (DataObject dataObjectToUnlock : dataObjects) {
			if (!dataObjectToUnlock.isLocked()) {
				lockDataObjects(unlockedDataObjects);
				IllegalDataObjectUnlockException e = new IllegalDataObjectUnlockException(dataObjectToUnlock);
				log.error(e.getMessage());
				throw e;
			}
			dataObjectToUnlock.unlock();
			unlockedDataObjects.add(dataObjectToUnlock);
		}
	}

	/**
	 * Get the {@link AtomicDataStateCondition}s of all unlocked {@link DataObject}s.
	 * 
	 * @return List of DataStateConditions which referring DataObject is
	 *         unlocked
	 */
	public List<AtomicDataStateCondition> getDataStateConditions() {
		List<AtomicDataStateCondition> existingConditions = new ArrayList<>();

		for (DataObject dataObject : dataObjectIdToDataObject.values()) {
			if (!dataObject.isLocked()) {
				existingConditions.add(dataObject.getCondition());
			}
		}
		return existingConditions;
	}

	/**
	 * Get all DataObjects that have the same DataStateCondition (equal
	 * DataClass and ObjectLifecycleState) as the conditions in the
	 * conditionSets.
	 * 
	 * @param conditionSets
	 *            - list of ConditionSets which DataStateCondition shall be
	 *            checked
	 * @return Set of DataObjects that have the same DataStateCondition as one
	 *         of DataStateConditions in the conditionSets
	 */
	public Set<DataObject> getAvailableDataObjects(List<ConditionSet> conditionSets) {
		Set<DataObject> availableDataObjects = new HashSet<>();
		for (ConditionSet conditionSet : conditionSets) {
			for (AtomicDataStateCondition condition : conditionSet.getConditions()) {
				Set<DataObject> availableDataObjectsForCondition = getAvailableDataObjects(condition);
				availableDataObjects.addAll(availableDataObjectsForCondition);
			}
		}

		return availableDataObjects;
	}

	/**
	 * Get all DataObjects that have the same DataStateCondition (equal
	 * DataClass and ObjectLifecycleState) as condition.
	 * 
	 * @param condition
	 * @return
	 */
	public Set<DataObject> getAvailableDataObjects(AtomicDataStateCondition condition) {
		Set<DataObject> availableDataObjects = new HashSet<>();

		for (DataObject dataObject : dataObjectIdToDataObject.values()) {
			if (condition.equals(dataObject.getCondition())) {
				availableDataObjects.add(dataObject);
			}
		}

		return availableDataObjects;
	}

	/**
	 * Set value of DataObjects.
	 * 
	 * @param dataAttributeValues
	 */
	public void setDataAttributeValues(Map<String, Map<String, Object>> dataAttributeValues) {
		for (Map.Entry<String, Map<String, Object>> transition : dataAttributeValues.entrySet()) {
			String dataObjectId = transition.getKey();
			if (!dataObjectIdToDataObject.containsKey(dataObjectId))
				continue;
			DataObject dataObject = dataObjectIdToDataObject.get(dataObjectId);
			Map<String, Object> attributeValues = transition.getValue();
			dataObject.setDataAttributeValues(attributeValues);
		}
	}

	/**
	 * Get a specific DataObject by id.
	 * 
	 * @param dataObjectId
	 * @return DataObject
	 */
	public DataObject getDataObjectById(String dataObjectId) {
		if (dataObjectIdToDataObject.containsKey(dataObjectId)) {
			return dataObjectIdToDataObject.get(dataObjectId);
		}
		IllegalDataObjectIdException e = new IllegalDataObjectIdException(dataObjectId);
		log.error(e.getMessage());
		throw e;
	}

	/**
	 * Get a List of specific DataObjects by their ids.
	 * 
	 * @param dataObjectIds
	 * @return List of DataObjects
	 */
	public List<DataObject> getDataObjectsById(List<String> dataObjectIds) {
		List<DataObject> dataObjects = new ArrayList<>();
		for (String dataObjectId : dataObjectIds) {
			try {
				DataObject dataObject = getDataObjectById(dataObjectId);
				dataObjects.add(dataObject);
			} catch (IllegalArgumentException e) {
				throw e;
			}
		}
		return dataObjects;
	}

	// GETTER & SETTER
	public DataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	public CaseExecutioner getCaseExecutioner() {
		return caseExecutioner;
	}

	public void setCaseExecutioner(CaseExecutioner caseExecutioner) {
		this.caseExecutioner = caseExecutioner;
	}

	public List<DataObject> getDataObjects() {
		Collection<DataObject> dataObjects = dataObjectIdToDataObject.values();
		return new ArrayList<>(dataObjects);
	}

	public Map<String, DataObject> getDataObjectIdToDataObject() {
		return dataObjectIdToDataObject;
	}

	/**
	 * 
	 * @return all DataAttributeInstances in all existing DataObjects
	 */
	public List<DataAttributeInstance> getDataAttributeInstances() {
		return this.getDataObjects().stream().map(DataObject::getDataAttributeInstances).flatMap(Collection::stream).collect(Collectors.toList());
	}
}