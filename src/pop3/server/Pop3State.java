package pop3.server;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public enum Pop3State
{
    /**
     * 
     */
    INITIALIZATION,
    /**
     * 
     */
    AUTHENTICATION,
    /**
     * 
     */
    TRANSACTION,
    /**
     * 
     */
    UPDATE;
}
