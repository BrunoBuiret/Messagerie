package smtp.server;

import smtp.SmtpProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import smtp.exceptions.SmtpConnectionInitializationException;
import smtp.server.commands.AbstractSmtpCommand;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class SmtpConnection extends Thread
{
    /**
     * A reference to the SMTP server.
     */
    protected SmtpServer server;

    /**
     * The connection' socket.
     */
    protected SSLSocket socket;

    /**
     * The connection's output stream.
     */
    protected BufferedOutputStream socketWriter;

    /**
     * The connection's input stream.
     */
    protected BufferedInputStream socketReader;

    /**
     * The connection's current state.
     */
    protected SmtpState currentState;

    /**
     * The sender buffer for transactions.
     */
    protected String senderBuffer;

    /**
     * The recipients buffer for transactions.
     */
    protected Set<String> recipientsBuffer;

    /**
     * The body buffer for transactions.
     */
    protected StringBuilder bodyBuffer;

    /**
     * Creates a new SMTP connection.
     * 
     * @param server A reference to the SMTP server.
     * @param socket The newly accepted socket.
     * @throws smtp.exceptions.SmtpConnectionInitializationException If the connection can't
     * be properly initialized.
     */
    public SmtpConnection(SmtpServer server, SSLSocket socket)
    throws SmtpConnectionInitializationException
    {
        // Initialize properties
        this.server = server;
        this.socket = socket;
        this.currentState = SmtpState.INITIALIZATION;
        this.senderBuffer = null;
        this.recipientsBuffer = null;
        this.bodyBuffer = null;

        // Set up socket
        try
        {
            // Determine which cipher suites can be used
            List<String> availableCipherSuites = new ArrayList<>(Arrays.asList(this.socket.getSupportedCipherSuites()));
            List<String> usableCipherSuites = new ArrayList<>();

            availableCipherSuites
                .stream()
                .filter((cipherSuite) -> (cipherSuite.contains("anon")))
                .forEach((cipherSuite) -> {
                    usableCipherSuites.add(cipherSuite);
                })
            ;

            this.socket.setEnabledCipherSuites(usableCipherSuites.toArray(new String[usableCipherSuites.size()]));

            // Start handshake
            this.socket.startHandshake();

            // Get streams
            this.socketWriter = new BufferedOutputStream(this.socket.getOutputStream());
            this.socketReader = new BufferedInputStream(this.socket.getInputStream());
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpConnection.class.getName()).log(
                Level.SEVERE,
                "Couldn't start connection socket.",
                ex
            );
            
            throw new SmtpConnectionInitializationException(ex);
        }
    }

    /**
     * The connection's main loop.
     */
    @Override
    public void run()
    {
        // Initialize vars
        StringBuilder responseBuilder;

        // Indicate the connection has been established
        try
        {
            // Build the greetings
            responseBuilder = new StringBuilder();
            responseBuilder.append("220 ");
            responseBuilder.append(this.server.getName());
            responseBuilder.append(" SMTP server ready");
            responseBuilder.append(SmtpProtocol.END_OF_LINE);

            // Then, send it
            this.sendResponse(responseBuilder.toString());

            // And set the next state
            this.currentState = SmtpState.EXPECTING_GREETINGS;
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpConnection.class.getName()).log(
                Level.SEVERE,
                "Couldn't send greetings.",
                ex
            );

            // Close the socket
            this.closeSocket();

            // Then, finish the thread
            return;
        }
        finally
        {
            // This builder isn't useful anymore
            responseBuilder = null;
        }

        // Initialize some more vars for the main loop
        String request;
        AbstractSmtpCommand command;
        boolean keepLooping = true;

        do
        {
            request = this.readRequest();

            if(null != request && !request.isEmpty())
            {
                // Extract the command from the request
                command = this.server.supportsCommand(SmtpProtocol.extractCommand(request));

                // Is the command supported?
                if(null != command)
                {
                    if(command.isValid(this))
                    {
                        // Handle the command
                        keepLooping = command.handle(this, request);
                    }
                    else
                    {
                        // The command is invalid because it can't be used right now
                        try
                        {
                            // Build the error response
                            responseBuilder = new StringBuilder();
                            responseBuilder.append("503 Bad sequence of commands");
                            responseBuilder.append(SmtpProtocol.END_OF_LINE);

                            // Then, send it
                            this.sendResponse(responseBuilder.toString());
                        }
                        catch(IOException ex)
                        {
                            Logger.getLogger(SmtpConnection.class.getName()).log(
                                Level.SEVERE,
                                "Couldn't send error response.",
                                ex
                            );
                        }
                        finally
                        {
                            // Finally, clear the builder
                            responseBuilder = null;
                        }
                    }
                }
                else
                {
                    try
                    {
                        // Build the error response
                        responseBuilder = new StringBuilder();
                        responseBuilder.append("500 Syntax error, command unrecognized");
                        responseBuilder.append(SmtpProtocol.END_OF_LINE);

                        // Then, send it
                        this.sendResponse(responseBuilder.toString());
                    }
                    catch(IOException ex)
                    {
                        Logger.getLogger(SmtpConnection.class.getName()).log(
                            Level.SEVERE,
                            "Couldn't send error response.",
                            ex
                        );
                    }
                    finally
                    {
                        // This builder isn't useful anymore
                        responseBuilder = null;
                    }
                }
            }
            else if(null == request)
            {
                keepLooping = false;
            }
        }
        while(keepLooping);

        // The loop has reached its end, close the socket and end the thread
        this.closeSocket();
    }

    /**
     * Factorized method to close the connection' socket.
     */
    protected void closeSocket()
    {
        try
        {
            this.socket.close();
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpConnection.class.getName()).log(
                Level.SEVERE,
                "Couldn't close socket.",
                ex
            );
        }
    }

    /**
     * Reads a request from the client by trying to read everything available
     * from the stream.
     * 
     * @return The client's request.
     */
    public String readRequest()
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

                if(-1 != readByte)
                {
                    dataWriter.writeByte(readByte);
                }
            }
            while(this.socketReader.available() > 0 && -1 != readByte);

            // Log if necessary
            if(this.server.isDebug())
            {
                Logger.getLogger(SmtpConnection.class.getName()).log(
                    Level.INFO,
                    "<- {0}:{1} {2}",
                    new Object[]
                    {
                        this.socket.getInetAddress(), this.socket.getPort(), new String(dataStream.toByteArray(), StandardCharsets.UTF_8).trim()
                    }
                );
            }

            // Get the byte array
            byte[] byteArray = dataStream.toByteArray();

            return byteArray.length > 0 ? new String(byteArray, StandardCharsets.UTF_8).trim() : null;
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpConnection.class.getName()).log(
                Level.SEVERE,
                "Couldn't read request from client.",
                ex
            );
        }

        return null;
    }

    /**
     * Reads data from the client until a specific pattern is found.
     * 
     * @param pattern The pattern to look for.
     * @return The client's request.
     */
    public String readUntil(String pattern)
    {
        return this.readUntil(pattern, StandardCharsets.UTF_8);
    }
    
    /**
     * Reads data from the client until a specific pattern is found.
     * 
     * @param pattern The pattern to look for.
     * @param charset The charset to build the string with.
     * @return The client's request.
     */
    public String readUntil(String pattern, Charset charset)
    {
        // Initialize vars
        ByteArrayOutputStream dataStream;
        DataOutputStream dataWriter = new DataOutputStream(dataStream = new ByteArrayOutputStream());
        int readByte;
        String currentData;

        try
        {
            do
            {
                // Append the next character to the buffer
                readByte = this.socketReader.read();

                if(-1 != readByte)
                {
                    dataWriter.writeByte(readByte);
                }

                // Build a string with the current data
                currentData = new String(dataStream.toByteArray(), charset);
            }
            while(currentData.endsWith(pattern));

            // Log if necessary
            if(this.server.isDebug())
            {
                Logger.getLogger(SmtpConnection.class.getName()).log(
                    Level.INFO,
                    "<- {0}:{1} {2}",
                    new Object[]
                    {
                        this.socket.getInetAddress(), this.socket.getPort(), currentData.trim()
                    }
                );
            }

            return currentData;
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpConnection.class.getName()).log(
                Level.SEVERE,
                "Couldn't read data from client.",
                ex
            );
        }

        return null;
    }

    /**
     * Sends a response to the client.
     * 
     * @param response The response to send.
     * @throws java.io.IOException If the response couldn't be sent.
     */
    public void sendResponse(String response)
    throws IOException
    {
        // Initialize vars
        ByteArrayOutputStream dataStream;
        DataOutputStream dataWriter = new DataOutputStream(dataStream = new ByteArrayOutputStream());

        try
        {
            // Transform the response into a byte array
            dataWriter.writeBytes(response);

            // Log if necessary
            if(this.server.isDebug())
            {
                Logger.getLogger(SmtpConnection.class.getName()).log(
                    Level.INFO,
                    "-> {0}:{1} {2}",
                    new Object[]
                    {
                        this.socket.getInetAddress(), this.socket.getPort(), response.trim()
                    }
                );
            }

            // Then, send the response to the client
            this.socketWriter.write(dataStream.toByteArray());
            this.socketWriter.flush();
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpConnection.class.getName()).log(
                Level.SEVERE,
                "Couldn't send response to the client.",
                ex
            );

            throw ex;
        }
    }

    /**
     * Gets the connection's reference to the server.
     *
     * @return The server.
     */
    public SmtpServer getServer()
    {
        return this.server;
    }

    /**
     * Gets the connection's current state.
     *
     * @return The current state.
     */
    public SmtpState getCurrentState()
    {
        return this.currentState;
    }

    /**
     * Sets the connection's current state.
     *
     * @param state The state.
     */
    public void setCurrentState(SmtpState state)
    {
        this.currentState = state;
    }

    /**
     * Gets the connection' sender buffer.
     * 
     * @return The sender buffer.
     */
    public String getSenderBuffer()
    {
        return this.senderBuffer;
    }

    /**
     * Sets the connection' sender buffer.
     * 
     * @param senderBuffer The sender buffer.
     */
    public void setSenderBuffer(String senderBuffer)
    {
        this.senderBuffer = senderBuffer;
    }

    /**
     * Gets the connection's recipients buffer.
     *
     * @return The recipients buffer.
     */
    public Set<String> getRecipientsBuffer()
    {
        if(null == this.recipientsBuffer)
        {
            this.recipientsBuffer = new HashSet<>();
        }

        return this.recipientsBuffer;
    }

    /**
     * Sets the connection's recipients buffer.
     * 
     * @param recipientsBuffer The recipients buffer.
     */
    public void setRecipientsBuffer(Set<String> recipientsBuffer)
    {
        this.recipientsBuffer = recipientsBuffer;
    }

    /**
     * Gets the connection's body buffer.
     * 
     * @return The body buffer.
     */
    public StringBuilder getBodyBuffer()
    {
        if(null == this.bodyBuffer)
        {
            this.bodyBuffer = new StringBuilder();
        }

        return this.bodyBuffer;
    }

    /**
     * Sets the connection's body buffer.
     * 
     * @param bodyBuffer The body buffer.
     */
    public void setBodyBuffer(StringBuilder bodyBuffer)
    {
        this.bodyBuffer = bodyBuffer;
    }
}
