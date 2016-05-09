package smtp.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class SmtpClientInitializationException extends AbstractSmtpException
{
    /**
     * {@inheritDoc}
     */
    public SmtpClientInitializationException(Throwable cause)
    {
        super(cause);
    }
}
