package de.uni_potsdam.hpi.bpt.bp2014.jcomparser.json;

import com.google.gson.Gson;
import de.uni_potsdam.hpi.bpt.bp2014.jcomparser.Connector;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents an EventType.
 */
public class EventType implements IPersistable { //IDeserialisableJson
    /**
     * This is the modelID of the eventType.
     */
    private long eventTypeModelID;
    /**
     * This is the databaseID of the eventType.
     */
    private int eventTypeID;
    /**
     * This is the name of the eventType.
     */
    private String eventTypeName;
    /**
     * This is a list containing all eventTypeAttributes belonging to this eventType.
     */
    private List<EventTypeAttribute> eventTypeAttributes = new LinkedList<>();
    /**
     * This is the databaseID of the scenario the eventType belongs to.
     */
    private int scenarioID;
    /**
     * This contains the JSON representation of the eventType.
     */
    private JSONObject eventTypeJson;
    /**
     * The URL of the Event Processing Platform where the Event Query will be registered.
     */
    private final String registrationUrl = "localhost:8080/Unicorn/REST/EventType";

    /**
     * The standard constructor.
     */
    public EventType() {
    }

    /**
     * Default constructor, sets the scenarioID for linking the eventType back to the scenario.
     *
     * @param scenarioID Database ID of the scenario.
     */
    public EventType(int scenarioID) {
        this.scenarioID = scenarioID;
    }

    /**
     * This initializes the eventType from the JSON.
     *
     * @param element The JSON String which will be used for deserialisation
     */
    //@Override
    public void initializeInstanceFromJson(final String element) {
        try {
            this.eventTypeJson = new JSONObject(element);

            this.eventTypeName = this.eventTypeJson.getString("name");
            this.eventTypeModelID = this.eventTypeJson.getLong("_id");
            generateEventTypeAttributeList(this.eventTypeJson.getJSONArray("attributes"));
            registerEventType();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //@Override public void initializeInstanceFromXML(final Node element) {

    //}

    /**
     * This method saves the eventType to the database.
     *
     * @return the databaseID of the eventType.
     */
    @Override public int save() {
        Connector conn = new Connector();
        this.eventTypeID = conn.insertEventTypeIntoDatabase(this.eventTypeName, this.scenarioID);
        saveEventTypeAttributes();

        return eventTypeID;
    }

    /**
     * This method gets all the eventTypeAttributes from the JSON.
     * EventTypeAttributes can only be alphanumerical.
     *
     * @param jsonAttributes This JSONArray contains all eventTypeAttributes from the JSON.
     */
    private void generateEventTypeAttributeList(JSONArray jsonAttributes) {
        int length = jsonAttributes.length();
        for (int i = 0; i < length; i++) {
            EventTypeAttribute eventTypeAttribute = new EventTypeAttribute(
                    jsonAttributes.getJSONObject(i).getString("name"),
                    jsonAttributes.getJSONObject(i).getString("datatype")
            );
            this.eventTypeAttributes.add(eventTypeAttribute);
        }
    }

    /**
     * This method iterates through all eventTypeAttributes and sets
     * the eventType for them as well as calling the save method.
     */
    private void saveEventTypeAttributes() {
        for (EventTypeAttribute eventTypeAttribute : this.eventTypeAttributes) {
            eventTypeAttribute.setEventTypeID(eventTypeID);
            eventTypeAttribute.save();
        }
    }

    /**
     * Register the Event Type in the Unicorn event processing platform.
     */
    private void registerEventType() {

        String xsd;
        String schemaName = getEventTypeName();
        EventTypeJsonObject json = new EventTypeJsonObject();
        Gson gson = new Gson();
        String timestampName = "";

        for(EventTypeAttribute eta : getEventTypeAttributes()) {
            if("timestamp".equals(eta.getEventTypeAttributeName())) {
                timestampName = "timestamp";
                break;
            }
        }

        xsd = generateXsd();

        json.setSchemaName(schemaName);
        json.setTimestampName(timestampName);
        json.setXsd(xsd);

        String jsonString = gson.toJson(json);
        Client client = ClientBuilder.newClient();
        client.target(getRegistrationUrl()).request(MediaType.APPLICATION_JSON)
                .post(Entity.json(jsonString));
;    }

    private String generateXsd() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        buffer.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"");
        buffer.append(getEventTypeName());
        buffer.append(".xsd\"\n");
        buffer.append("targetNamespace=\"");
        buffer.append(getEventTypeName());
        buffer.append(".xsd\" elementFormDefault=\"qualified\">\n");
        buffer.append("<xs:element name=\"");
        buffer.append(getEventTypeName());
        buffer.append("\">\n");
        buffer.append("<xs:complexType>\n");
        buffer.append("<xs:sequence>\n");
        for(EventTypeAttribute eta : getEventTypeAttributes()) {
            buffer.append("<xs:element name=\"");
            buffer.append(eta.getEventTypeAttributeName());
            buffer.append("\" type=\"xs:");
            buffer.append(eta.getEventTypeAttributeType());
            buffer.append("\"\nminOccurs=\"1\" maxOccurs=\"1\" />\n");
        }
        buffer.append("</xs:sequence>\n");
        buffer.append("</xs:complexType>\n");
        buffer.append("</xs:element>\n");
        buffer.append("</xs:schema>");

        return buffer.toString();
    }

    private class EventTypeJsonObject {
        private String xsd;
        private String schemaName;
        private String timestampName;

        public String getTimestampName() {
            return timestampName;
        }

        public void setTimestampName(String timestampName) {
            this.timestampName = timestampName;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getXsd() {
            return xsd;
        }

        public void setXsd(String xsd) {
            this.xsd = xsd;
        }
    }

    public int getEventTypeID() {
        return eventTypeID;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public List<EventTypeAttribute> getEventTypeAttributes() {
        return eventTypeAttributes;
    }


    public JSONObject getEventTypeJson() {
        return eventTypeJson;
    }

    public long getEventTypeModelID() {
        return eventTypeModelID;
    }

    public String getRegistrationUrl() {
        return registrationUrl;
    }

}