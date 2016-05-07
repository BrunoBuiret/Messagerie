package smtp.server.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import smtp.SmtpProtocol;
import smtp.server.SmtpConnection;
import smtp.server.SmtpState;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class ExtendedHelloCommand extends AbstractSmtpCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(SmtpConnection connection)
    {
        return connection.getCurrentState().equals(SmtpState.EXPECTING_GREETINGS);
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
        responseBuilder.append("250 ");
        responseBuilder.append(connection.getServer().getName());
        responseBuilder.append(" greets ");
        
        if(request.length() > 4 && !request.substring(4).trim().isEmpty())
        {
            responseBuilder.append(request.substring(4).trim());
        }
        else
        {
            responseBuilder.append("client");
        }
        
        responseBuilder.append(SmtpProtocol.END_OF_LINE);
        
        // Then, send it
        try
        {
            connection.sendResponse(responseBuilder.toString());
            
            // And set the next state
            connection.setCurrentState(SmtpState.EXPECTING_GREETINGS);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ExtendedHelloCommand.class.getName()).log(
                Level.SEVERE,
                "Greetings response couldn't be sent.",
                ex
            );
        }
        
        return true;
    }
}
