package de.uni_potsdam.hpi.bpt.bp2014.jcore.data;

import de.uni_potsdam.hpi.bpt.bp2014.database.data.DbDataNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class InputSet extends DataConditions {
    public InputSet(int inputSetId) {
        DbDataNode dbDataNode = new DbDataNode();
        this.dataClassToState = dbDataNode.getDataClassIdToState(inputSetId);
    }

    public boolean isFulfilled(List<DataObject> dataObjects) {
        dataObjects = dataObjects.stream().filter(x -> !x.isLocked())
                .collect(Collectors.toList());
        return super.checkConditions(dataObjects);
    }
}
