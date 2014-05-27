package cl.cc.pojo;

import java.util.Date;

/**
 *
 * @author CyberCastle
 */
public class MetadataProperties {

    private Long procedureNumber;
    private String procedureType;
    private Date procedureStart;
    private Date procedureEnd;
    private String thematicArea;
    private String petitionerId;
    private String petitionerName;
    private String documentName;
    private String originSystem;
    private String processedStatus;
    private String errorMessage;

    public Long getProcedureNumber() {
        return procedureNumber;
    }

    public void setProcedureNumber(Long procedureNumber) {
        this.procedureNumber = procedureNumber;
    }

    public String getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(String procedureType) {
        this.procedureType = procedureType;
    }

    public Date getProcedureStart() {
        return procedureStart;
    }

    public void setProcedureStart(Date procedureStart) {
        this.procedureStart = procedureStart;
    }

    public Date getProcedureEnd() {
        return procedureEnd;
    }

    public void setProcedureEnd(Date procedureEnd) {
        this.procedureEnd = procedureEnd;
    }

    public String getThematicArea() {
        return thematicArea;
    }

    public void setThematicArea(String thematicArea) {
        this.thematicArea = thematicArea;
    }

    public String getPetitionerId() {
        return petitionerId;
    }

    public void setPetitionerId(String petitionerId) {
        this.petitionerId = petitionerId;
    }

    public String getPetitionerName() {
        return petitionerName;
    }

    public void setPetitionerName(String petitionerName) {
        this.petitionerName = petitionerName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getOriginSystem() {
        return originSystem;
    }

    public void setOriginSystem(String originSystem) {
        this.originSystem = originSystem;
    }

    public String getProcessedStatus() {
        return processedStatus;
    }

    public void setProcessedStatus(String processedStatus) {
        this.processedStatus = processedStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
