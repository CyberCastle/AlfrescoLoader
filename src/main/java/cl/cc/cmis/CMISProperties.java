package cl.cc.cmis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CyberCastle
 */
public class CMISProperties {

    private String namespace;
    private String documentName;
    private Boolean folder;
    private final List<String> aspects;
    private final Map<String, Object> properties;

    public CMISProperties() {
        this.aspects = new ArrayList<>();
        this.properties = new HashMap<>();
        this.folder = false;
    }

    public void addAspect(String aspect) {
        this.aspects.add(aspect);
    }

    public void addProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Boolean isFolder() {
        return folder;
    }

    public void setFolder(Boolean folder) {
        this.folder = folder;
    }

    public List<String> getAspects() {
        return aspects;
    }

    // A este método solo puede acceder la clase CMISHandler
    Map<String, Object> getCMISPropertiesMap() {

        String baseObjectTypeId = "cmis:document";
        if (this.folder) {
            baseObjectTypeId = "cmis:folder";
        }

        for (String aspect : this.aspects) {
            baseObjectTypeId += ",P:" + this.namespace + ":" + aspect;
        }

        Map<String, Object> _properties = new HashMap();
        _properties.put("cmis:name", this.documentName);
        _properties.put("cmis:objectTypeId", baseObjectTypeId);

        for (Map.Entry<String, Object> property : this.properties.entrySet()) {
            _properties.put(this.namespace + ":" + property.getKey(), property.getValue());
        }
        
        return _properties;
    }
}
