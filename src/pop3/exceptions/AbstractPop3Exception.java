package pop3.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class AbstractPop3Exception extends RuntimeException
{
    /**
     * Creates a new <code>AbstractPop3Exception</code> with the given cause.
     * 
     * @param cause The exception's cause.
     */
    public AbstractPop3Exception(Throwable cause)
    {
        super(cause);
    }
}
