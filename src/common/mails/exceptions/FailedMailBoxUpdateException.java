package common.mails.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class FailedMailBoxUpdateException extends AbstractMailException
{
    /**
     * {@inheritDoc}
     */
    public FailedMailBoxUpdateException(String message)
    {
        super(message);
    }
    
    /**
     * {@inheritDoc}
     */
    public FailedMailBoxUpdateException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
