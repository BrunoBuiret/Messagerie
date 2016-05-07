package pop3.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class Pop3ConnectionInitializationException extends AbstractPop3Exception
{
    /**
     * {@inheritDoc}
     */
    public Pop3ConnectionInitializationException(Throwable cause)
    {
        super(cause);
    }
}
