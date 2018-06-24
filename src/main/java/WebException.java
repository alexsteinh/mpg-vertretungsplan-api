/**
 * Exception for WebClient
 */
public class WebException extends Exception
{
    /**
     * Basic constructor
     */
    public WebException()
    {
        super();
    }

    /**
     * Constructor with massage parameter
     *
     * @param message Message which describes error
     */
    public WebException(String message)
    {
        super(message);
    }

    /**
     * Constructor with message and cause parameter
     *
     * @param message Message which describes error
     * @param cause Throwable which contains message
     */
    public WebException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
