package cl.cc.cmis;

/**
 *
 * @author CyberCastle
 */
public class CMISHandlerException extends Exception {

    private static final long serialVersionUID = -9072748588901423500L;
    private final String exceptionCode;

    public String getExceptionCode() {
        return this.exceptionCode;
    }

    public CMISHandlerException(String message, String exceptionCode) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    public CMISHandlerException(String message, String exceptionCode, Exception e) {
        super(message, e);
        this.exceptionCode = exceptionCode;
    }
}
