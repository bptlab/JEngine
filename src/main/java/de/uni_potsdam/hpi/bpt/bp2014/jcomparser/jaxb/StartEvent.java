package de.uni_potsdam.hpi.bpt.bp2014.jcomparser.jaxb;

import de.uni_potsdam.hpi.bpt.bp2014.jcomparser.AbstractControlNode;
import de.uni_potsdam.hpi.bpt.bp2014.jcomparser.Connector;

import javax.xml.bind.annotation.*;

/**
 *
 */
@XmlRootElement(name = "bpmn:startEvent")
@XmlAccessorType(XmlAccessType.NONE)
public class StartEvent extends AbstractControlNode {
    @XmlAttribute(name = "id")
    private String id;
    @XmlAttribute(name = "name")
    private String name = "";
    @XmlElement(name = "bpmn:outgoing")
    private String outgoing;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(String outgoing) {
        this.outgoing = outgoing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int save() {
        Connector connector = new Connector();
        this.databaseId = connector.insertControlNodeIntoDatabase(
                this.getName(), "Startevent", this.getFragmentId(), this.id);
        return this.databaseId;
    }
}
