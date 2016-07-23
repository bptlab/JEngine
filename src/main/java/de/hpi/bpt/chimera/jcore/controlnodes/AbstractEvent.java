package de.hpi.bpt.chimera.jcore.controlnodes;

import de.hpi.bpt.chimera.database.controlnodes.DbControlNodeInstance;
import de.hpi.bpt.chimera.database.controlnodes.events.DbEvent;
import de.hpi.bpt.chimera.jcore.ScenarioInstance;
import de.hpi.bpt.chimera.jcore.eventhandling.EventDispatcher;
import de.hpi.bpt.chimera.jcore.eventhandling.SseNotifier;
import de.hpi.bpt.chimera.jcore.executionbehaviors.EventExecutionBehavior;
import de.hpi.bpt.chimera.jcore.flowbehaviors.EventIncomingBehavior;
import de.hpi.bpt.chimera.jcore.flowbehaviors.EventOutgoingBehavior;

/**
 *
 */
public abstract class AbstractEvent extends AbstractControlNodeInstance {
    private int controlNodeId;
    private String queryString;
    private EventOutgoingBehavior outgoingBehavior;

    /**
     * Builds a new instance of an AbstractEvent.
     * @param controlNodeId the id of the event control node.
     * @param fragmentInstanceId the ud of the instance the event belongs to.
     * @param scenarioInstance the scenarioInstance object.
     */
    public AbstractEvent(
            int controlNodeId, int fragmentInstanceId, ScenarioInstance scenarioInstance) {
        this.controlNodeId = controlNodeId;
        this.scenarioInstance = scenarioInstance;
        this.setFragmentInstanceId(fragmentInstanceId);
        this.setExecutionBehavior(new EventExecutionBehavior(this));
        this.setIncomingBehavior(new EventIncomingBehavior(this));

        DbControlNodeInstance databaseNodeInstance = new DbControlNodeInstance();
        if (!databaseNodeInstance.existControlNodeInstance(controlNodeId, fragmentInstanceId)) {
            int controlNodeInstanceId = databaseNodeInstance.createNewControlNodeInstance(
                    controlNodeId, this.getType(), fragmentInstanceId);
            this.setControlNodeInstanceId(controlNodeInstanceId);
        } else {
            this.setControlNodeInstanceId(databaseNodeInstance.getControlNodeInstanceId(
                    controlNodeId, fragmentInstanceId));
        }
        outgoingBehavior = this.createOutgoingBehavior();
    }

    protected EventOutgoingBehavior createOutgoingBehavior() {
        return new EventOutgoingBehavior(controlNodeId,
                scenarioInstance, getFragmentInstanceId(), getControlNodeInstanceId());
    }

    public abstract String getType();


    @Override
    public EventOutgoingBehavior getOutgoingBehavior() {
        return this.outgoingBehavior;
    }

    public String getQueryString() {
        return queryString;
    }

    @Override
    public void terminate() {
        terminate("");
    }

    /**
     * Terminate the event and set the data attributes accordingly. Send refresh to the
     * Chimera frontend.
     *
     * @param eventJson the json string containing the values.
     */
    public void terminate(String eventJson) {
        outgoingBehavior.terminate(eventJson);
    }
}
