package cl.cc.main;

import cl.cc.cmis.CMISHandler;
import cl.cc.cmis.CMISProperties;
import cl.cc.config.ConfigHandler;
import cl.cc.pojo.MetadataProperties;
import cl.cc.reporting.ExcelReport;
import cl.cc.utils.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author CyberCastle
 */
public class Run {

    private static final LinkedList<String> propertiesToRead;
    private static final LinkedList<String> propertiesToWrite;
    private static final LinkedList<String> headerSheetResult;

    static {
        propertiesToRead = new LinkedList<>(Arrays.asList("procedureNumber",
                "procedureType",
                "procedureStart",
                "procedureEnd",
                "thematicArea",
                "petitionerId",
                "petitionerName",
                "documentName"));

        propertiesToWrite = new LinkedList<>(Arrays.asList("procedureNumber",
                "documentName",
                "processedStatus",
                "errorMessage"));

        headerSheetResult = new LinkedList<>(Arrays.asList("Número de trámite",
                "Nombre de Documento",
                "Cargado Correctamente",
                "Descripción del Error"));
    }

    public static void main(String... arg) throws Exception {
        // Documentos de origen
        String inputExcelPath = "/Users/CyberCastle/tmp/FilesToLoad.xlsx";
        String outputExcelPath = "/Users/CyberCastle/tmp/FilesToLoad.xlsx";
        String documentsBasePath = "/Users/CyberCastle/tmp";

        // Cargamos el Excel con los documentos a cargar 
        ExcelReport report = new ExcelReport();
        report.setBaseDocument(inputExcelPath);
        report.loadBase();
        report.setFirstRow(1);
        report.setWorkingSheet(0);

        // Configuramos el handler de CMIS y abrimos la conexión
        CMISHandler cmisHdlr = new CMISHandler(ConfigHandler.getProperty("cmis.access.url"));
        cmisHdlr.setCredentials(ConfigHandler.getProperty("cmis.access.usr"), ConfigHandler.getProperty("cmis.access.pwd"));
        cmisHdlr.openConnection();

        // Seteamos la carpeta raíz
        String rootFolderID = cmisHdlr.getObjectIdByPath(ConfigHandler.getProperty("cmis.base.folder"));

        List<MetadataProperties> lst = report.readSheet(propertiesToRead, MetadataProperties.class);
        for (MetadataProperties property : lst) {
            System.out.println(property.getProcedureNumber() + "\t" + property.getProcedureStart());

            String procedureTypeFolderID = cmisHdlr.createFolder("UYD", rootFolderID);
            String procedureYearFolderID = cmisHdlr.createFolder("2013", procedureTypeFolderID);

            String documentName = property.getDocumentName() + property.getProcedureNumber() + ".pdf";
            String filePath = documentsBasePath + "/" + documentName;
            String fileType = "application/pdf";
            File f = new File(filePath);

            // Seteamos la metadata del documento
            CMISProperties properties = new CMISProperties();
            properties.setDocumentName(documentName);
            properties.setNamespace("mssp");
            properties.addAspect("tramites_electronicos");
            properties.addProperty("num_prestacion", property.getProcedureNumber());
            properties.addProperty("tipo_prestacion", property.getProcedureType());
            // Adecuación de las fechas para CMIS
            GregorianCalendar startDate = new GregorianCalendar();
            GregorianCalendar endDate = new GregorianCalendar();
            startDate.setTime(property.getProcedureStart());
            endDate.setTime(property.getProcedureEnd());
            // --
            properties.addProperty("fecha_inicio", startDate);
            properties.addProperty("fecha_termino", endDate);            
            properties.addProperty("area_tematica", property.getThematicArea());
            properties.addProperty("rut", property.getPetitionerId());
            properties.addProperty("nombre_solicitante", property.getPetitionerName());
            properties.addProperty("origen_sistema", "STL");

            try {
                String documentID = cmisHdlr.createDocument(procedureYearFolderID, properties, fileType, f);
                System.out.println("Documento procesado: " + documentID);

                // Capturamos el resultado de la carga
                property.setProcessedStatus("Si");
                property.setErrorMessage("Without Error");

            } catch (Exception e) {
                // Capturamos el resultado  y errores de la carga
                property.setProcessedStatus("No");
                property.setErrorMessage(Util.ExceptionTraceToString(e));
            }
        }

        report.addSheet("Resultado Carga");
        report.setFirstColumn(0);
        report.setFirstRow(0);
        report.addSheetHeader(headerSheetResult);
        report.setFirstRow(1);
        report.fillSheet(lst, propertiesToWrite);
        report.save(new FileOutputStream(outputExcelPath));
        cmisHdlr.closeConnection();
    }
}
