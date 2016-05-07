package common.mails.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class AbstractMailException extends RuntimeException
{
    /**
     * Creates a new <code>AbstractMailException</code> with the given message.
     * 
     * @param message The exception's message.
     */
    public AbstractMailException(String message)
    {
        super(message);
    }
    
    /**
     * Creates a new <code>AbstractMailException</code> with the given message
     * and given cause.
     * 
     * @param message The exception's message.
     * @param cause The exception's cause.
     */
    public AbstractMailException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
