package pl.mcybulski.XMLConfig.exceptions;

/**
 * Created by Mikołaj on 2015-01-10.
 */
public class UnrecognizedTokenException extends Exception {

    public final static String ERROR_DESC = "Unrecognized token occured";

    public UnrecognizedTokenException() {
        super(ERROR_DESC);
    }

    public UnrecognizedTokenException(String message) {
        super(message);
    }
}
