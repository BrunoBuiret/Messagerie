package smtp.server;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public enum SmtpState
{
    /**
     * The connection is being initialized.
     */
    INITIALIZATION,
    /**
     * The server is expecting greetings from the client.
     */
    EXPECTING_GREETINGS,
    /**
     * The server is expecting the start of a transaction from the client.
     */
    EXPECTING_TRANSACTION,
    /**
     * The server is expecting the list of recipitients from the client.
     */
    EXPECTING_RECIPIENTS,
    /**
     * The server is expecting the mail's body from the client.
     */
    EXPECTING_BODY;
}
