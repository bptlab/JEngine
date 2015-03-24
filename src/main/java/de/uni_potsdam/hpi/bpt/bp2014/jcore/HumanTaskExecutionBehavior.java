package de.uni_potsdam.hpi.bpt.bp2014.jcore;


import java.util.Map;

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


public class HumanTaskExecutionBehavior extends TaskExecutionBehavior {

    public HumanTaskExecutionBehavior(int activityInstance_id, ScenarioInstance scenarioInstance, ControlNodeInstance controlNodeInstance) {
        super(activityInstance_id, scenarioInstance, controlNodeInstance);
    }

    //diese Methode wird immer aufgerufen wenn eine Aktivität gestartet wird.
    //Hier können eventuell sonderfälle behandelt werden.
    @Override
    public void execute() {
        //darf erst true werden wenn alle attribute richtig gesetzt wurden.
        //denn erst dann kann die aktivität terminieren
        ((ActivityInstance)controlNodeInstance).setCanTerminate(true);
    }

    @Override
    public void setDataAttributeValues(Map<Integer, String> values){
        for(Integer i : values.keySet()){
            DataAttributeInstance dataAttributeInstance = scenarioInstance.getDataAttributeInstances().get(i);
            dataAttributeInstance.setValue(i, values.get(i));
        }
        ((ActivityInstance)controlNodeInstance).setCanTerminate(true);
    }


}
