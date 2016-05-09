package smtp.server.commands;

import common.mails.Mail;
import common.mails.MailBox;
import common.mails.exceptions.FailedMailBoxUpdateException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
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
        // Initialize vars
        StringBuilder responseBuilder = new StringBuilder();
        
        // Is there at least one valid recipient?
        if(connection.getRecipientsBuffer().size() > 0)
        {
            // Build response to notice the client
            responseBuilder.append("354 Start mail input; end with <CRLF>.<CRLF>");
            responseBuilder.append(SmtpProtocol.END_OF_LINE);

            // Then, send the response
            try
            {
                connection.sendResponse(responseBuilder.toString());

                // And set the next state
                connection.setCurrentState(SmtpState.EXPECTING_BODY);
            }
            catch(IOException ex)
            {
                Logger.getLogger(DataCommand.class.getName()).log(
                    Level.SEVERE,
                    "Start of data response couldn't be sent.",
                    ex
                );
            }
            
            if(connection.getCurrentState().equals(SmtpState.EXPECTING_BODY))
            {
                // Try reading the body
                String data = connection.readUntil(SmtpProtocol.END_OF_DATA);
                
                if(null != data)
                {
                    // Body has been successfully read, save it
                    connection.setBodyBuffer(data);
                    
                    // Initialize some more vars
                    Set<String> recipientsBuffer = connection.getRecipientsBuffer();
                    MailBox mailBox;
                    Mail mail = Mail.parse(data);
                    boolean errorHappened = false;
                    
                    // Add the mail to every recipient
                    for(String recipient : recipientsBuffer)
                    {
                        mailBox = connection.getServer().getMailBox(recipient);
                        
                        if(null != mailBox)
                        {
                            mailBox.add(mail);
                            
                            try
                            {
                                mailBox.save();
                            }
                            catch(IllegalArgumentException | FailedMailBoxUpdateException ex)
                            {
                                Logger.getLogger(DataCommand.class.getName()).log(
                                    Level.SEVERE,
                                    "Couldn't save mailbox.",
                                    ex
                                );
                                
                                errorHappened = true;
                            }
                            catch(FileNotFoundException ex)
                            {
                                // This exception isn't supposed to happen
                            }
                        }
                    }
                    
                    // And set the next state
                    connection.setCurrentState(SmtpState.EXPECTING_TRANSACTION);
                    
                    // Then, send a response
                    responseBuilder = new StringBuilder();
                    responseBuilder.append(
                        !errorHappened
                            ? "250 OK"
                            : "451 Requested action aborted: local error in processing"
                    );
                    responseBuilder.append(SmtpProtocol.END_OF_LINE);
                    
                    try
                    {
                        connection.sendResponse(responseBuilder.toString());
                    }
                    catch(IOException ex)
                    {
                        Logger.getLogger(DataCommand.class.getName()).log(
                            Level.SEVERE,
                            "End of data response couldn't be sent.",
                            ex
                        );
                    }
                }
                else
                {
                    
                }
            }
        }
        else
        {
            // Build response
            responseBuilder.append("554 No valid recipients");
            responseBuilder.append(SmtpProtocol.END_OF_LINE);

            // Then, send it
            try
            {
                connection.sendResponse(responseBuilder.toString());
            }
            catch(IOException ex)
            {
                Logger.getLogger(DataCommand.class.getName()).log(
                    Level.SEVERE,
                    "Data error response couldn't be sent.",
                    ex
                );
            }
        }
        
        return true;
    }
}
