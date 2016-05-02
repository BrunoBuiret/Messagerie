package smtp.server.commands;

import smtp.server.SmtpConnection;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public abstract class AbstractSmtpCommand
{
    /**
     * Tests if the command can be used at this moment.
     *
     * @param connection A reference to the connection.
     * @return <code>true</code> if the command is valid, <code>false</code>
     * otherwise.
     */
    public abstract boolean isValid(SmtpConnection connection);

    /**
     * Handles the request.
     *
     * @param connection A reference to the connection.
     * @param request The request to handle.
     * @return <code>true</code> to keep looping, <code>false</code> otherwise.
     */
    public abstract boolean handle(SmtpConnection connection, String request);
}
