package pop3.server;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public enum Pop3State
{
    /**
     * The connection is being initialized.
     */
    INITIALIZATION,
    /**
     * The server is waiting for the client to authenticate themselves.
     */
    AUTHENTICATION,
    /**
     * The client has authenticated themselves and is retrieving mail.
     */
    TRANSACTION,
    /**
     * The mailbox is being updated.
     */
    UPDATE;
}
