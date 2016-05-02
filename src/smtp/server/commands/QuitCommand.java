package smtp.server.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import smtp.SmtpProtocol;
import smtp.server.SmtpConnection;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class QuitCommand extends AbstractSmtpCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(SmtpConnection connection)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handle(SmtpConnection connection, String request)
    {
        // Initialize vars
        StringBuilder responseBuilder = new StringBuilder();
        
        // Build response
        responseBuilder.append("221 ");
        responseBuilder.append(connection.getServer().getName());
        responseBuilder.append(" Service closing transmission channel");
        
        responseBuilder.append(SmtpProtocol.END_OF_LINE);
        
        // Then, send it
        try
        {
            connection.sendResponse(responseBuilder.toString());
        }
        catch(IOException ex)
        {
            Logger.getLogger(ExtendedHelloCommand.class.getName()).log(
                Level.SEVERE,
                "Quit response couldn't be sent.",
                ex
            );
        }
        
        return false;
    }
}
