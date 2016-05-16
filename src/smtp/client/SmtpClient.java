package smtp.client;

import common.mails.Mail;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import smtp.SmtpProtocol;
import smtp.exceptions.SmtpClientInitializationException;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class SmtpClient
{
    /**
     *
     */
    protected enum SmtpState
    {
        Initialisation,
        Connected,
        MailTransaction,
        WaitForData,
        DataTransaction,
        EndDataTransaction,
        WaitForExitConfirm;
    };

    /**
     * The socket.
     */
    protected SSLSocket socket;

    /**
     * The socket's output stream.
     */
    protected BufferedOutputStream socketWriter;

    /**
     * The socket's input stream.
     */
    protected BufferedInputStream socketReader;

    /**
     * The current state of the client.
     */
    protected SmtpState currentState = SmtpState.Initialisation;

    /**
     * Creates a new SMTP client.
     *
     * @param host The server's host.
     * @param port The server's port.
     * @throws smtp.exceptions.SmtpClientInitializationException If the client can't
     * be properly initialized.
     */
    public SmtpClient(InetAddress host, int port)
    throws SmtpClientInitializationException
    {
        try
        {
            // Initialize vars
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

            // Create socket
            this.socket = (SSLSocket) factory.createSocket(host, port);

            // Determine which cipher suites can be used
            this.socket.setEnabledCipherSuites(this.socket.getSupportedCipherSuites());

            // Start handshake
            this.socket.startHandshake();

            // Get streams
            this.socketWriter = new BufferedOutputStream(this.socket.getOutputStream());
            this.socketReader = new BufferedInputStream(this.socket.getInputStream());

            // Reads the greetings from the server
            this.readResponse();
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(
                Level.SEVERE,
                "Couldn't initialize SMTP client.",
                ex
            );
            
            throw new SmtpClientInitializationException(ex);
        }
    }

    /**
     * Reads a response from the server.
     *
     * @return The server's response.
     */
    protected String readResponse()
    {
        // Initialize vars
        ByteArrayOutputStream dataStream;
        DataOutputStream dataWriter = new DataOutputStream(dataStream = new ByteArrayOutputStream());
        int readByte;

        try
        {
            // Try reading everything
            do
            {
                readByte = this.socketReader.read();

                if (-1 != readByte)
                {
                    dataWriter.writeByte(readByte);
                }
            }
            while(this.socketReader.available() > 0 && -1 != readByte);

            // Get the byte array
            byte[] byteArray = dataStream.toByteArray();

            return byteArray.length > 0 ? new String(byteArray).trim() : null;
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(
                Level.SEVERE,
                "Couldn't read response from server.",
                ex
            );
        }

        return null;
    }

    /**
     * Sends a request to the server.
     *
     * @param request The request
     * @throws java.io.IOException If the request can't be sent.
     */
    protected void sendRequest(String request)
    throws IOException
    {
        // Initialize vars
        ByteArrayOutputStream dataStream;
        DataOutputStream dataWriter = new DataOutputStream(dataStream = new ByteArrayOutputStream());

        try
        {
            // Transform the response into a byte array
            dataWriter.writeBytes(request);

            // Then, send the response to the client
            this.socketWriter.write(dataStream.toByteArray());
            this.socketWriter.flush();
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(
                Level.SEVERE,
                "Couldn't send request to the server.",
                ex
            );

            throw ex;
        }
    }

    /**
     * Validates a response from the server with the expected one.
     *
     * @param futureState The next state if everything goes smoothly.
     * @param serverResponse The response.
     * @return A response code.
     */
    protected int stateValidation(SmtpState futureState, String serverResponse)
    {
        if(serverResponse.startsWith("250"))
        {
            this.currentState = futureState;

            return 1;
        }
        else if(serverResponse.startsWith("354"))
        {
            this.currentState = futureState;

            return 2;
        }
        else if(serverResponse.startsWith("221"))
        {
            this.currentState = futureState;

            return 3;
        }

        return 0;
    }

    /**
     * Sends an extended hello greetings.
     *
     * @param domain The client's domain or IP address.
     * @return A response code.
     */
    public int ehlo(String domain)
    {
        try
        {
            this.sendRequest("EHLO " + domain + SmtpProtocol.END_OF_LINE);
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.MailTransaction, this.readResponse());
    }

    /**
     * Starts a transaction.
     *
     * @param mailAddress The sender's mail address.
     * @return A response code.
     * @todo Check greetings have already been sent.
     */
    public int mailFrom(String mailAddress)
    {
        try
        {
            this.sendRequest("MAIL FROM:<" + mailAddress + ">" + SmtpProtocol.END_OF_LINE);
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.WaitForData, this.readResponse());
    }

    /**
     * Adds a recipient to the current transaction.
     *
     * @param recipient The recipient's mail address.
     * @return A response code.
     * @todo Check there is a transaction.
     */
    public int rcptTo(String recipient)
    {
        try
        {
            this.sendRequest("RCPT TO:<" + recipient + ">" + SmtpProtocol.END_OF_LINE);
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.DataTransaction, this.readResponse());
    }

    /**
     * Notifies the server the next data which will be sent is going to be the mail's body.
     *
     * @return A response code.
     */
    public int data()
    {
        try
        {
            this.sendRequest("DATA" + SmtpProtocol.END_OF_LINE);
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.EndDataTransaction, this.readResponse());
    }

    /**
     * Sends the mail's body.
     *
     * @param body The mail's body.
     * @return A response code.
     */
    public int sendMailBody(String body)
    {
        // Add the end of data pattern to parse the mail if there are headers
        if(!body.endsWith(SmtpProtocol.END_OF_DATA))
        {
            body += SmtpProtocol.END_OF_DATA;
        }
        
        // Build an email to facilitate the send
        Mail mail = Mail.parse(body);
        
        // Initialize vars to build fragments
        StringBuilder mailBuilder = new StringBuilder();
        Map<String, String> headers = mail.getHeaders();
        List<String> bodyFragments = new ArrayList<>();
        int currentIndex = 0, bodyLength = mail.getBody().length();
        
        // Write headers if there is at least one
        if(!headers.isEmpty())
        {
            for(Map.Entry<String, String> entry : headers.entrySet())
            {
                mailBuilder.append(entry.getKey());
                mailBuilder.append(": ");
                mailBuilder.append(entry.getValue());
                mailBuilder.append("\r\n");
            }

            // Write separator
            mailBuilder.append("\r\n");
        }
        
        // Write body
        while(currentIndex < bodyLength)
        {
            if(bodyLength - currentIndex >= 76)
            {
                bodyFragments.add(
                    mail.getBody().substring(
                        currentIndex, currentIndex + 76
                    )
                );

                currentIndex += 76;
            }
            else
            {
                bodyFragments.add(
                    mail.getBody().substring(
                        currentIndex
                    )
                );
                currentIndex = mail.getBody().length();
            }
        }
        
        // Rebuild every line
        mailBuilder.append(
            String.join(
                "\r\n",
                bodyFragments
            )
        );
        mailBuilder.append(SmtpProtocol.END_OF_DATA);
        
        // Finally, send it
        try
        {
            this.sendRequest(mailBuilder.toString());
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.WaitForExitConfirm, this.readResponse());
    }

    /**
     * Closes the connection.
     *
     * @return A response code.
     */
    public int quit()
    {
        try
        {
            this.sendRequest("QUIT" + SmtpProtocol.END_OF_LINE);
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.stateValidation(SmtpState.Initialisation, this.readResponse());
    }
}
