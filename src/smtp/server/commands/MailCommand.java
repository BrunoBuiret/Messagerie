package smtp.server.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import smtp.SmtpProtocol;
import smtp.server.SmtpConnection;
import smtp.server.SmtpState;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class MailCommand extends AbstractSmtpCommand
{
    /**
     * The command pattern to fetch the sender for this transaction.
     *
     * @see http://emailregex.com/
     */
    protected static final Pattern COMMAND_PATTERN = Pattern.compile(
        "MAIL FROM:<((?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\]))>"
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(SmtpConnection connection)
    {
        return connection.getCurrentState().equals(SmtpState.EXPECTING_TRANSACTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handle(SmtpConnection connection, String request)
    {
        // Initialize vars
        StringBuilder responseBuilder = new StringBuilder();

        // Is the syntax valid?
        if(request.startsWith("MAIL FROM:"))
        {
            // Has the sender's email been given?
            Matcher matcher = MailCommand.COMMAND_PATTERN.matcher(request);

            if(matcher.matches())
            {
                // Store the sender in the associated buffer
                connection.setSenderBuffer(matcher.group(1));

                // And clear the others
                connection.setBodyBuffer(null);
                connection.setRecipientsBuffer(null);

                // Build response
                responseBuilder.append("250 OK");
                responseBuilder.append(SmtpProtocol.END_OF_LINE);
            }
            else
            {
                // Inform the user the email is invalid
                responseBuilder.append("501 Syntax error in parameters or arguments");
                responseBuilder.append(SmtpProtocol.END_OF_LINE);
            }
        }
        else
        {
            // Inform the user the syntax is incorrect
            responseBuilder.append("501 Syntax error in parameters or arguments");
            responseBuilder.append(SmtpProtocol.END_OF_LINE);
        }

        // Then, send the response
        try
        {
            connection.sendResponse(responseBuilder.toString());

            // And set the next state
            connection.setCurrentState(SmtpState.EXPECTING_RECIPIENTS);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ExtendedHelloCommand.class.getName()).log(
                Level.SEVERE,
                "Start of transaction response couldn't be sent.",
                ex
            );
        }

        return true;
    }
}
