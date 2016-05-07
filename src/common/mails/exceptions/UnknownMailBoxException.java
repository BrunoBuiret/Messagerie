package common.mails.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class UnknownMailBoxException extends AbstractMailException
{
    /**
     * {@inheritDoc}
     */
    public UnknownMailBoxException(String message)
    {
        super(message);
    }
    
    /**
     * {@inheritDoc}
     */
    public UnknownMailBoxException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
