package common.mails.exceptions;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class MarkedForDeletionException extends AbstractMailException
{
    /**
     * {@inheritDoc}
     */
    public MarkedForDeletionException(String message)
    {
        super(message);
    }
}
