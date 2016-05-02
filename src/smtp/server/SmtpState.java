package smtp.server;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public enum SmtpState
{
    /**
     * 
     */
    INITIALIZATION,
    /**
     * 
     */
    EXPECTING_GREETINGS,
    /**
     * 
     */
    EXPECTING_TRANSACTION,
    /**
     * 
     */
    EXPECTING_RECIPIENTS,
    /**
     * 
     */
    EXPECTING_BODY;
}
