package pop3.server.commands;

import common.mails.Mail;
import common.mails.MailBox;
import common.mails.exceptions.MarkedForDeletionException;
import common.mails.exceptions.NonExistentMailException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pop3.Pop3Protocol;
import pop3.server.Pop3Connection;
import pop3.server.Pop3State;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class ListCommand extends AbstractPop3Command
{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(Pop3Connection connection)
    {
        return connection.getCurrentState().equals(Pop3State.TRANSACTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handle(Pop3Connection connection, String request)
    {
        // Initialize vars
        StringBuilder responseBuilder = new StringBuilder();
        MailBox mailBox = connection.getMailBox();
        
        if(null != mailBox)
        {
            // Is there an argument?
            if(request.length() > 4)
            {
                // A mail number has been provided
                try
                {
                    int index = Integer.parseInt(request.substring(5).trim());
                    
                    if(index > 0)
                    {
                        try
                        {
                            // Try fetching the mail
                            Mail mail = connection.getMailBox().get(index - 1);
                            
                            // Inform the user the mail has been marked for deletion
                            responseBuilder.append(Pop3Protocol.RESPONSE_ERROR);
                            responseBuilder.append(" ");
                            responseBuilder.append(index);
                            responseBuilder.append(" ");
                            responseBuilder.append(mail.getSize());
                            responseBuilder.append(Pop3Protocol.END_OF_LINE);
                        }
                        catch(MarkedForDeletionException ex)
                        {
                            // Inform the user the mail has already been marked for deletion
                            responseBuilder.append(Pop3Protocol.RESPONSE_ERROR);
                            responseBuilder.append(" message ");
                            responseBuilder.append(index);
                            responseBuilder.append(" deleted");
                            responseBuilder.append(Pop3Protocol.END_OF_LINE);
                        }
                        catch(NonExistentMailException ex)
                        {
                            // Inform the user the mail doesn't exist
                            responseBuilder.append(Pop3Protocol.RESPONSE_ERROR);
                            responseBuilder.append(" no such message");
                            responseBuilder.append(Pop3Protocol.END_OF_LINE);
                        }
                    }
                    else
                    {
                        // Inform the user the index is invalid
                        responseBuilder.append(Pop3Protocol.RESPONSE_ERROR);
                        responseBuilder.append(" invalid mail number");
                        responseBuilder.append(Pop3Protocol.END_OF_LINE);
                    }
                }
                catch(NumberFormatException ex)
                {
                    // Inform the user the mail index couldn't be extracted
                    responseBuilder.append(Pop3Protocol.RESPONSE_ERROR);
                    responseBuilder.append(" couldn't extract mail index");
                    responseBuilder.append(Pop3Protocol.END_OF_LINE);
                }
            }
            else
            {
                // No mail number has been provided
                int mailsSize = 0, mailsNumber = 0;
                List<Mail> mailsList = mailBox.getAll();
                
                for(Mail mail : mailsList)
                {
                    if(!mailBox.isDeleted(mail))
                    {
                        mailsSize += mail.getSize();
                        mailsNumber++;
                    }
                }
                
                // Build response
                responseBuilder.append(Pop3Protocol.RESPONSE_OK);
                responseBuilder.append(" maildrop has ");
                responseBuilder.append(mailsNumber);
                responseBuilder.append(" ");
                responseBuilder.append(mailsNumber > 1 ? "messages" : "message");
                responseBuilder.append(" (");
                responseBuilder.append(mailsSize);
                responseBuilder.append(" ");
                responseBuilder.append(mailsSize > 1 ? "octets" : "octet");
                responseBuilder.append(")");
                responseBuilder.append(Pop3Protocol.END_OF_LINE);
                
                for(int i = 0, j = mailsList.size(); i < j; i++)
                {
                    if(!mailBox.isDeleted(mailsList.get(i)))
                    {
                        responseBuilder.append(i + 1);
                        responseBuilder.append(" ");
                        responseBuilder.append(mailsList.get(i).getSize());
                        responseBuilder.append(Pop3Protocol.END_OF_LINE);
                    }
                }

                responseBuilder.append(".");
                responseBuilder.append(Pop3Protocol.END_OF_LINE);
            }
        }
        else
        {
            // Build error response
            responseBuilder.append(Pop3Protocol.RESPONSE_ERROR);
            responseBuilder.append(" no mailbox associated");
            responseBuilder.append(Pop3Protocol.END_OF_LINE);
        }
        
        // Then, send response
        try
        {
            connection.sendResponse(responseBuilder.toString());
        }
        catch(IOException ex)
        {
            Logger.getLogger(ListCommand.class.getName()).log(
                Level.SEVERE,
                "Mails list response couldn't be sent.",
                ex
            );
        }
        
        return true;
    }
}
