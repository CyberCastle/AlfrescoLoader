package cl.cc.reporting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author CyberCastle
 */
public abstract class AbstractReport {

    protected InputStream baseDocument;

    public void setBaseDocument(InputStream baseDocument) {
        this.baseDocument = baseDocument;
    }

    public void setBaseDocument(String baseDocumentPath) throws  ReportException {
        try {
            this.baseDocument = new FileInputStream(baseDocumentPath);
        } catch (FileNotFoundException e) {
            throw new ReportException("Error, File in path '" + baseDocumentPath + "' not exist.", "Rprt - 01", e);
        }
    }

    public abstract void loadBase() throws ReportException;
    
    public abstract void save(OutputStream out) throws ReportException;
    
}
