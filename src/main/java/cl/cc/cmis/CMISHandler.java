package cl.cc.cmis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

/**
 *
 * @author CyberCastle
 */
public class CMISHandler {

    private Session session;
    private String user;
    private String passwd;
    private final String urlRepository;

    public CMISHandler(String urlRepository) {
        this.urlRepository = urlRepository;
    }

    public void setCredentials(String user, String passwd) {
        this.user = user;
        this.passwd = passwd;
    }

    // Método para abrir una conección
    public void openConnection() throws CMISHandlerException {
        try {
            if (this.user == null || this.passwd == null) {
                throw new CMISHandlerException("CMISM-90", "Username and password cannot be null");
            }

            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
            Map<String, String> parameters = new HashMap<>();

            // User credentials.
            parameters.put(SessionParameter.USER, this.user);
            parameters.put(SessionParameter.PASSWORD, this.passwd);

            // Connection settings.
            parameters.put(SessionParameter.ATOMPUB_URL, this.urlRepository);
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameters.put(SessionParameter.AUTH_HTTP_BASIC, "true");
            parameters.put(SessionParameter.COOKIES, "true");

            // Set the alfresco object factory
            parameters.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

            // Create session.
            // Alfresco only provides one repository.
            Repository repository = sessionFactory.getRepositories(parameters).get(0);
            this.session = repository.createSession();
        } catch (CmisConnectionException e) {
            throw new CMISHandlerException("CMISM-01", "Error connecting to repository", e);
        }
    }

    // Obtener el ID a traves del path de un objeto
    public String getObjectIdByPath(String objectPath) throws CMISHandlerException {
        try {
            return this.session.getObjectByPath(objectPath).getId();
        } catch (CmisObjectNotFoundException e) {
            throw new CMISHandlerException("CMISM-02", "Object does not exist", e);
        }
    }

    // Crea una carpeta, si es que no existe. Devuelve su Id
    @SuppressWarnings("unchecked")
    public String createFolder(String folderName, String parentFolderId) throws CMISHandlerException {
        String createdFolderId = this.getFolderIdByName(folderName, parentFolderId);
        try {
            if (createdFolderId == null) {
                Folder parentFolder = (Folder) this.session.getObject(parentFolderId);
                Map<String, Object> properties = new HashMap<>();
                properties.put(PropertyIds.NAME, folderName);
                properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
                createdFolderId = parentFolder.createFolder(properties).getId();
            }
        } catch (CmisContentAlreadyExistsException ex) {
            try {
                //
                Thread.sleep(5000l);
            } catch (InterruptedException e) {
                throw new CMISHandlerException("CMISM-05", "Error to create folder, this already exists", e);
            }
            createdFolderId =  this.createFolder(folderName, parentFolderId);
        }
        return createdFolderId;
    }

    // Crea un documento
    public String createDocument(String parentFolderId, CMISProperties properties, String mimeType, File documentFile) throws CMISHandlerException {
        String createdDocumentId = null;
        try {
            Folder parentFolder = (Folder) this.session.getObject(parentFolderId);

            ContentStream cs = null;
            if (documentFile != null && mimeType != null) {
                cs = this.session.getObjectFactory().createContentStream(documentFile.getName(), documentFile.length(), mimeType, new FileInputStream(documentFile));
            }

            createdDocumentId = parentFolder.createDocument(properties.getCMISPropertiesMap(), cs, VersioningState.MAJOR).getId();
        } catch (CmisContentAlreadyExistsException e) {
            throw new CMISHandlerException("CMISM-05", "Error to create document, this already exists", e);
        } catch (FileNotFoundException e) {
            throw new CMISHandlerException("CMISM-06", "Error to create document, file is not accesible", e);
        }
        return createdDocumentId;
    }

    // Obtenemos el contendio de un documento
    public InputStream getContentStream(String objectId) {
        Document document = (Document) this.session.getObject(objectId);
        ContentStream documentContent = document.getContentStream();
        return documentContent.getStream();
    }

    // Obtenemos la metadata de un documento
    public Map<String, Object> getMetadata(String objectId) throws CMISHandlerException {
        try {
            Map<String, Object> propertiesMap = new HashMap<>();
            for (Property<?> property : this.session.getObject(objectId).getProperties()) {
                propertiesMap.put(property.getId(), property.getValue());
            }
            return propertiesMap;
        } catch (CmisObjectNotFoundException e) {
            throw new CMISHandlerException("CMISM-02", "Object does not exist", e);
        }
    }

    // Método encargado de actualizar un documento, ya sea su contenido y/o su metadata
    public void updateDocument(String objectId, Boolean isMajor, String updateComments, Map<String, ?> properties, String mimeType, File documentFile) throws CMISHandlerException {
        try {
            Document documentToUpdate = (Document) this.session.getObject(objectId);
            if (documentToUpdate.getAllowableActions().getAllowableActions().contains(Action.CAN_CHECK_OUT)) {

                String documentName = documentToUpdate.getContentStream().getFileName();
                Document checkedOutDocument = (Document) this.session.getObject(documentToUpdate.checkOut());
                ContentStream cs = null;
                if (documentFile != null && mimeType != null) {
                    cs = this.session.getObjectFactory().createContentStream(documentName, documentFile.length(), mimeType, new FileInputStream(documentFile));
                }
                checkedOutDocument.checkIn(isMajor, properties, cs, updateComments, null, null, null);
            }
        } catch (FileNotFoundException e) {
            throw new CMISHandlerException("CMISM-03", "Error to load file", e);
        } catch (CmisObjectNotFoundException e) {
            throw new CMISHandlerException("CMISM-02", "Object does not exist", e);
        }
    }

    // Método encargado de actualizar sólo el contenido de un documento
    public void updateContent(String objectId, Boolean isMajor, String updateComments, String mimeType, File documentFile) throws CMISHandlerException {
        this.updateDocument(objectId, isMajor, updateComments, null, mimeType, documentFile);
    }

    // Método encargado de actualizar sólo la metadata de un documento
    public void updateMetadata(String objectId, Boolean isMajor, String updateComments, Map<String, ?> properties) throws CMISHandlerException {
        this.updateDocument(objectId, isMajor, updateComments, properties, null, null);
    }

    // Método encargado de actualizar sólo la metadata de un documento, sin versionar
    public void updateMetadata(String objectId, Map<String, Object> properties) throws CMISHandlerException {
        Document documentToUpdate = (Document) this.session.getObject(objectId);
        documentToUpdate.updateProperties(properties, false);
    }

    //Método para cerrar conección y liberar recursos
    public void closeConnection() {
        this.session.clear();
        this.session = null;
    }

    // Método para buscar el ID de una carpeta dentro de otra a través de su nombre
    public String getFolderIdByName(String objectName, String parentFolderId) throws CMISHandlerException {
        return this.getObjectIdByName(objectName, parentFolderId, "cmis:folder");
    }

    // Método para buscar el ID de un documento dentro de una carpeta a través de su nombre
    public String getDocumentIdByName(String objectName, String parentFolderId) throws CMISHandlerException {
        return this.getObjectIdByName(objectName, parentFolderId, "cmis:document");
    }

    // Método para buscar un ID, utilizando comandos Solr
    private String getObjectIdByName(String objectName, String parentFolderId, String type) throws CMISHandlerException {
        String queryString = "SELECT cmis:objectId FROM " + type + " WHERE in_tree('" + parentFolderId + "') AND cmis:name = '" + objectName + "'";

        // Execute query
        ItemIterable<QueryResult> results = this.session.query(queryString, false);
        if (results.getTotalNumItems() > 1) {
            throw new CMISHandlerException("CMISM-04", "Too many objects in 'object id by name' query.");
        }

        String objectID = null;
        if (results.iterator().hasNext()) {
            objectID = results.iterator().next().getPropertyValueByQueryName("cmis:objectId");
        }
        return objectID;
    }
}
