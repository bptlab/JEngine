package de.uni_potsdam.hpi.bpt.bp2014.database;

/**
 * Created by jaspar.mang on 19.03.15.
 */
public class DbDataAttributeInstance extends DbObject{

    public int createNewDataAttributeInstance(int dataAttribute_id, Object value, int dataObjectInstance_id) {
        String sql = "INSERT INTO dataattributeinstance (value, dataobjectinstance_id, dataattribute_id) VALUES ('" + value + "', " + dataObjectInstance_id + ", " + dataAttribute_id + ")";
        int id = this.executeInsertStatement(sql);
        return id;
    }

    public Boolean existDataAttributeInstance(int dataAttribute_id, int dataObjectInstance_id) {
        String sql = "SELECT id FROM dataattributeinstance WHERE dataobjectinstance_id = " + dataObjectInstance_id + " AND dataattribute_id = " + dataAttribute_id;
        return this.executeExistStatement(sql);
    }

    public int getDataAttributeInstanceID(int dataAttribute_id, int dataObjectInstance_id) {
        String sql = "SELECT id FROM dataattributeinstance WHERE dataobjectinstance_id = " + dataObjectInstance_id + " AND dataattribute_id = " + dataAttribute_id;
        return this.executeStatementReturnsInt(sql, "id");
    }

    public String getType(int dataAttribute_id) {
        String sql = "SELECT type FROM dataattribute WHERE id = " + dataAttribute_id;
        return this.executeStatementReturnsString(sql, "id");
    }

    public Object getValue(int dataAttributeInstance_id) {
        String sql = "SELECT value FROM dataattributeinstance WHERE id = " + dataAttributeInstance_id;
        return this.executeStatementReturnsObject(sql, "id");
    }

    //TODO: dataAttributes methods to set values
}
