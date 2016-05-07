package smtp.server.commands;

import smtp.server.SmtpConnection;
import smtp.server.SmtpState;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class DataCommand extends AbstractSmtpCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(SmtpConnection connection)
    {
        return connection.getCurrentState().equals(SmtpState.EXPECTING_RECIPIENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handle(SmtpConnection connection, String request)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
