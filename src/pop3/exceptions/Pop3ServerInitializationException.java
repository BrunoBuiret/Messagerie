package pop3.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class Pop3ServerInitializationException extends AbstractPop3Exception
{
    /**
     * {@inheritDoc}
     */
    public Pop3ServerInitializationException(Throwable cause)
    {
        super(cause);
    }
}
