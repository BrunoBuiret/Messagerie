package smtp.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class AbstractSmtpException extends RuntimeException
{
    /**
     * Creates a new <code>AbstractSmtpException</code> with the given cause.
     * 
     * @param cause The exception's cause.
     */
    public AbstractSmtpException(Throwable cause)
    {
        super(cause);
    }
}
