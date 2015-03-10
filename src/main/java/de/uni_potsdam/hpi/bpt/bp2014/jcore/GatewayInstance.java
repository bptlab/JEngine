package de.uni_potsdam.hpi.bpt.bp2014.jcore;

import de.uni_potsdam.hpi.bpt.bp2014.database.DbControlNode;
import de.uni_potsdam.hpi.bpt.bp2014.database.DbControlNodeInstance;
import de.uni_potsdam.hpi.bpt.bp2014.database.DbGatewayInstance;

/**
 * ********************************************************************************
 * <p/>
 * _________ _______  _        _______ _________ _        _______
 * \__    _/(  ____ \( (    /|(  ____ \\__   __/( (    /|(  ____ \
 * )  (  | (    \/|  \  ( || (    \/   ) (   |  \  ( || (    \/
 * |  |  | (__    |   \ | || |         | |   |   \ | || (__
 * |  |  |  __)   | (\ \) || | ____    | |   | (\ \) ||  __)
 * |  |  | (      | | \   || | \_  )   | |   | | \   || (
 * |\_)  )  | (____/\| )  \  || (___) |___) (___| )  \  || (____/\
 * (____/   (_______/|/    )_)(_______)\_______/|/    )_)(_______/
 * <p/>
 * ******************************************************************
 * <p/>
 * Copyright © All Rights Reserved 2014 - 2015
 * <p/>
 * Please be aware of the License. You may found it in the root directory.
 * <p/>
 * **********************************************************************************
 */

public class GatewayInstance extends ControlNodeInstance {
    private Boolean isXOR;
    private Boolean isAND;
    private ScenarioInstance scenarioInstance;
    /**
     * Database Connection objects.
     */
    private final DbControlNodeInstance dbControlNodeInstance = new DbControlNodeInstance();
    private final DbControlNode dbControlNode = new DbControlNode();
    private final DbGatewayInstance dbGatewayInstance = new DbGatewayInstance();

    /**
     * Creates and initializes a new gateway instance.
     * Reads the information for an existing gateway instance from the database or creates a new one if no one
     * exist in the database.
     *
     * @param controlNode_id      This is the id of the control node.
     * @param fragmentInstance_id This is the id of the fragment instance.
     * @param scenarioInstance    This is an instance from the class ScenarioInstance.
     */
    public GatewayInstance(int controlNode_id, int fragmentInstance_id, ScenarioInstance scenarioInstance) {
        //looks if the Gateway Instance has already been initialized
        for (ControlNodeInstance controlNodeInstance : scenarioInstance.getControlNodeInstances()) {
            if (controlNodeInstance.fragmentInstance_id == controlNodeInstance_id && controlNodeInstance.controlNode_id == controlNode_id) {
                //if it exist, only checks the control flow
                controlNodeInstance.incomingBehavior.enableControlFlow();
                return;
            }
        }
        this.scenarioInstance = scenarioInstance;
        this.controlNode_id = controlNode_id;
        this.fragmentInstance_id = fragmentInstance_id;
        scenarioInstance.getControlNodeInstances().add(this);
        String type = dbControlNode.getType(controlNode_id);
        if (type.equals("AND")) {
            this.isAND = true;
            this.isXOR = false;
        } else if(type.equals("XOR")) {
            this.isAND = false;
            this.isXOR = true;
        }
        if (dbControlNodeInstance.existControlNodeInstance(controlNode_id, fragmentInstance_id)) {
            //initializes all Gateway Instances in the database
            this.controlNodeInstance_id = dbControlNodeInstance.getControlNodeInstanceID(controlNode_id, fragmentInstance_id);
        } else {
            //creates a new Gateway Instance also in database
            if (isAND) {
                this.controlNodeInstance_id = dbControlNodeInstance.createNewControlNodeInstance(controlNode_id, "AND", fragmentInstance_id);
            } else if (isXOR) {
                this.controlNodeInstance_id = dbControlNodeInstance.createNewControlNodeInstance(controlNode_id, "XOR", fragmentInstance_id);
            }
            if (isAND) {
                dbGatewayInstance.createNewGatewayInstance(controlNodeInstance_id, "AND", "init");
            } else if (isXOR) {
                dbGatewayInstance.createNewGatewayInstance(controlNodeInstance_id, "XOR", "init");
            }
        }
        this.stateMachine = new GatewayStateMachine(controlNode_id, scenarioInstance, this);
        if (isAND) {
            this.outgoingBehavior = new ParallelGatewaySplitBehavior(controlNode_id, scenarioInstance, fragmentInstance_id);
            this.incomingBehavior = new ParallelGatewayJoinBehavior(this, scenarioInstance);
        } else if(isXOR) {
            this.outgoingBehavior = new ExclusiveGatewaySplitBehavior(controlNode_id, scenarioInstance, fragmentInstance_id);
            this.incomingBehavior = new ExclusiveGatewayJoinBehavior(this, scenarioInstance, stateMachine);
        }
    }

    public boolean checkTermination(int controlNode_id){
        return ((ExclusiveGatewaySplitBehavior)outgoingBehavior).checkTermination(controlNode_id);
    }

    /**
     * Terminates the gateway instance.
     * Set the state in database.
     */
    @Override
    public boolean terminate() {
        stateMachine.terminate();
        outgoingBehavior.terminate();
        return true;
    }

    @Override
    public boolean skip() {
        return stateMachine .skip();
    }

    /**
     * Getter
     */

    public Boolean getIsXOR() {
        return isXOR;
    }

    public Boolean getIsAND() {
        return isAND;
    }

    public ScenarioInstance getScenarioInstance() {
        return scenarioInstance;
    }
}
