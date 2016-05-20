package de.uni_potsdam.hpi.bpt.bp2014.jcore.data;

import de.uni_potsdam.hpi.bpt.bp2014.database.data.DbDataClass;
import de.uni_potsdam.hpi.bpt.bp2014.database.data.DbDataObjectInstance;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.ScenarioInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents data object instances.
 */
public class DataObjectInstance {

	private final int dataObjectInstanceId;
	private final int dataObjectId;
	private final int scenarioId;
	private final int scenarioInstanceId;
	private final String name;

	/**
	 * Database Connection objects.
	 */
	private final ScenarioInstance scenarioInstance;
	private final DbDataObjectInstance dbDataObjectInstance = new DbDataObjectInstance();
	private int stateId;
	private List<DataAttributeInstance> dataAttributeInstances = new ArrayList<>();
    private boolean isLocked = false;
	/**
	 * Creates and initializes a new data object instance.
	 * Reads the information for an existing data object instance from the database
	 * or creates a new one if no one exist in the database.
	 *
	 * @param dataObjectId       This is the database id from the data object instance.
	 * @param scenarioId         This is the database id from the scenario.
	 * @param scenarioInstanceId This is the database id from the scenario instance.
	 * @param scenarioInstance    This is an instance from the class ScenarioInstance.
	 */
	public DataObjectInstance(int dataObjectId, int scenarioId, int scenarioInstanceId,
			ScenarioInstance scenarioInstance) {
		this.scenarioInstance = scenarioInstance;
		this.dataObjectId = dataObjectId;
		this.scenarioId = scenarioId;
		this.scenarioInstanceId = scenarioInstanceId;
        DbDataClass dataClass = new DbDataClass();
		this.name = dataClass.getName(dataObjectId);
		if (dbDataObjectInstance.existDataObjectInstance(scenarioInstanceId, dataObjectId)) {
			dataObjectInstanceId = dbDataObjectInstance
					.getDataObjectInstanceID(scenarioInstanceId, dataObjectId);
			this.stateId = dbDataObjectInstance.getStateID(dataObjectInstanceId);
            isLocked = dbDataObjectInstance.isLocked(dataObjectInstanceId);
		} else {
			this.dataObjectInstanceId = dbDataObjectInstance
					.createNewDataObjectInstance(
							scenarioInstanceId,
							stateId,
							dataObjectId);
		}
		this.initializeAttributes();
	}

	private void initializeAttributes() {
        DbDataClass dataClass = new DbDataClass();
        List<Integer> dataAttributeIds = dataClass
				.getDataAttributesForDataObject(dataObjectId);
        for (int dataAttributeId : dataAttributeIds) {
			DataAttributeInstance dataAttributeInstance = new DataAttributeInstance(
					dataAttributeId, dataObjectInstanceId, this);
            dataAttributeInstances.add(dataAttributeInstance);
			scenarioInstance.getDataAttributeInstances()
					.put(dataAttributeInstance.getDataAttributeInstanceId(),
							dataAttributeInstance);
		}
	}

	/**
	 * Sets the state for the data object instance in database and attribute.
	 *
	 * @param stateId This is the new state id.
	 */
	public void setState(int stateId) {
		this.stateId = stateId;
		dbDataObjectInstance.setState(dataObjectInstanceId, stateId);
		scenarioInstance.checkTerminationCondition();
	}

	/**
	 * Getter.
	 * @return the Scenario Instance.
	 */
	public ScenarioInstance getScenarioInstance() {
		return scenarioInstance;
	}

	/**
	 * @return the ID of the Scenario Instance.
	 */
	public int getScenarioInstanceId() {
		return scenarioInstanceId;
	}

	/**
	 * @return the ID of the Scenario.
	 */
	public int getScenarioId() {
		return scenarioId;
	}

	/**
	 * @return the ID of the Data Object.
	 */
	public int getDataObjectId() {
		return dataObjectId;
	}

	/**
	 * @return the ID of this Data Object Instance.
	 */
	public int getDataObjectInstanceId() {
		return dataObjectInstanceId;
	}

	/**
	 * @return the ID of the state.
	 */
	public int getStateId() {
		return stateId;
	}

	/**
	 * @return the Name of this Data Object Instance.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return a list containing the Data Attribute Instances belonging
	 * to this Data Object Instance.
	 */
	public List<DataAttributeInstance> getDataAttributeInstances() {
		return dataAttributeInstances;
	}

    public void lock() {
        this.dbDataObjectInstance.setLocked(this.dataObjectInstanceId, true);
        this.isLocked = true;
    }

    public void unlock() {
        this.dbDataObjectInstance.setLocked(this.dataObjectInstanceId, false);
        this.isLocked = false;
    }

    public boolean isLocked () {
        return this.isLocked;
    }

}
