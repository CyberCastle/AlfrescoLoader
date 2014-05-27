package cl.cc.reporting;

/**
 *
 * @author CyberCastle
 */
public class ReportException extends Exception {
    
    private static final long serialVersionUID = -5529316039930224761L;
    private final String exceptionCode;

    public String getExceptionCode() {
        return this.exceptionCode;
    }

    public ReportException(String message, String exceptionCode) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    public ReportException(String message, String exceptionCode, Exception e) {
        super(message, e);
        this.exceptionCode = exceptionCode;
    }
}
