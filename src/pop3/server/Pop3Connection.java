package pop3.server;

import common.mails.MailBox;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import pop3.Pop3Protocol;
import pop3.server.commands.AbstractPop3Command;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class Pop3Connection extends Thread
{
    /**
     * 
     */
    protected Pop3Server server;
    
    /**
     * 
     */
    protected SSLSocket socket;
    
    /**
     * 
     */
    protected BufferedOutputStream socketWriter;
    
    /**
     * 
     */
    protected BufferedInputStream socketReader;
    
    /**
     * 
     */
    protected Pop3State currentState;
    
    /**
     * 
     */
    protected MailBox mailBox;
    
    /**
     * 
     */
    protected String securityDigest;
    
    /**
     * 
     * @param server
     * @param socket 
     */
    public Pop3Connection(Pop3Server server, SSLSocket socket)
    {
        // Initialize properties
        this.server = server;
        this.socket = socket;
        this.currentState = Pop3State.INITIALIZATION;
        this.mailBox = null;
        this.securityDigest = null;
        
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
            });
            
            this.socket.setEnabledCipherSuites(usableCipherSuites.toArray(new String[usableCipherSuites.size()]));
            
            // Start handshake
            this.socket.startHandshake();
            
            // Get streams
            this.socketWriter = new BufferedOutputStream(this.socket.getOutputStream());
            this.socketReader = new BufferedInputStream(this.socket.getInputStream());
        }
        catch(IOException ex)
        {
            // @todo Throw exception to avoid {@code #run()} being executed.
            Logger.getLogger(Pop3Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     */
    @Override
    public void run()
    {
        // Initialize some vars
        StringBuilder responseBuilder;
        
        // Indicate the connection has been established
        try
        {
            // Build the greetings
            responseBuilder = new StringBuilder();
            responseBuilder.append(Pop3Protocol.RESPONSE_OK);
            responseBuilder.append(" ");
            responseBuilder.append(this.server.getName());
            responseBuilder.append(" POP3 server ready");
            
            // Compute security digest if possible
            try
            {
                // Initialize vars
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                String processName = ManagementFactory.getRuntimeMXBean().getName();
                int processId = Integer.parseInt(processName.substring(0, processName.indexOf("@")));
                String host = processName.substring(processName.indexOf("@") + 1);
                long clock = System.currentTimeMillis();

                // Build security digest
                StringBuilder digestBuilder = new StringBuilder();
                byte[] rawSecurityDigest = md5.digest(
                    String.format(
                        "<%d.%d@%s>%s",
                        processId,
                        clock,
                        host,
                        this.server.getSecret()
                    ).getBytes(StandardCharsets.UTF_8)
                );

                for(byte b : rawSecurityDigest)
                {
                    digestBuilder.append(String.format("%02x", b & 0xff));
                }

                this.securityDigest = digestBuilder.toString();
                
                // Add the data needed to build the security digest for the client
                responseBuilder.append(" <");
                responseBuilder.append(processId);
                responseBuilder.append(".");
                responseBuilder.append(clock);
                responseBuilder.append("@");
                responseBuilder.append(host);
                responseBuilder.append(">");
            }
            catch(NoSuchAlgorithmException ex)
            {
                // MD5 message digest couldn't be fetched, disable APOP command
                this.securityDigest = null;

                Logger.getLogger(Pop3Connection.class.getName()).log(
                    Level.SEVERE,
                    "Couldn't fetch MD5 message digest.",
                    ex
                );
            }
            
            // End the greetings
            responseBuilder.append(Pop3Protocol.END_OF_LINE);

            // Then, send it
            this.sendResponse(responseBuilder.toString());
            
            // Finally, clear the builder
            responseBuilder = null;

            // And set the next state
            this.currentState = Pop3State.AUTHENTICATION;
        }
        catch (IOException ex)
        {
            Logger.getLogger(Pop3Connection.class.getName()).log(
                Level.SEVERE,
                "Couldn't send greetings.",
                ex
            );
            
            // Close the socket
            this.closeSocket();

            // Then, finish the thread
            return;
        }
        
        // Initialize some more vars for the main loop
        String request;
        AbstractPop3Command command;
        boolean keepLooping = true;
        
        // Main loop
        do
        {
            // Read the client's request
            request = this.readRequest();
            
            if(null != request && !request.isEmpty())
            {
                // Extract the command from the request
                command = this.server.supportsCommand(Pop3Protocol.extractCommand(request));
                
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
                            responseBuilder.append(Pop3Protocol.RESPONSE_ERROR);
                            responseBuilder.append(" invalid command");
                            responseBuilder.append(Pop3Protocol.END_OF_LINE);

                            // Then, send it
                            this.sendResponse(responseBuilder.toString());
                        }
                        catch(IOException ex)
                        {
                            Logger.getLogger(Pop3Connection.class.getName()).log(
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
                        responseBuilder.append(Pop3Protocol.RESPONSE_ERROR);
                        responseBuilder.append(" unknown command");
                        responseBuilder.append(Pop3Protocol.END_OF_LINE);

                        // Then, send it
                        this.sendResponse(responseBuilder.toString());
                    }
                    catch(IOException ex)
                    {
                        Logger.getLogger(Pop3Connection.class.getName()).log(
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
     * 
     */
    protected void closeSocket()
    {
        try
        {
            this.socket.close();
        }
        catch(IOException ex)
        {
            Logger.getLogger(Pop3Connection.class.getName()).log(
                Level.SEVERE,
                "Couldn't close socket.",
                ex
            );
        }
    }
    
    /**
     * 
     * @return 
     */
    protected String readRequest()
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
                Logger.getLogger(Pop3Connection.class.getName()).log(
                    Level.INFO,
                    "<- {0}:{1} {2}",
                    new Object[]
                    {
                        this.socket.getInetAddress(), this.socket.getPort(), new String(dataStream.toByteArray()).trim()
                    }
                );
            }

            // Get the byte array
            byte[] byteArray = dataStream.toByteArray();

            return byteArray.length > 0 ? new String(byteArray, StandardCharsets.UTF_8).trim() : null;
        }
        catch(IOException ex)
        {
            Logger.getLogger(Pop3Connection.class.getName()).log(
                Level.SEVERE,
                "Couldn't read request from client.",
                ex
            );
        }

        return null;
    }
    
    /**
     * 
     * @param response 
     * @throws java.io.IOException 
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
                Logger.getLogger(Pop3Connection.class.getName()).log(
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
            Logger.getLogger(Pop3Connection.class.getName()).log(
                Level.SEVERE,
                "Couldn't send response to the client.",
                ex
            );

            throw ex;
        }
    }
    
    /**
     * Gets a connection's reference to the server.
     *
     * @return The server.
     */
    public Pop3Server getServer()
    {
        return this.server;
    }

    /**
     * Gets a connection's current state.
     *
     * @return The current state.
     */
    public Pop3State getCurrentState()
    {
        return this.currentState;
    }

    /**
     * Sets a connection's current state.
     *
     * @param state The state.
     */
    public void setCurrentState(Pop3State state)
    {
        this.currentState = state;
    }

    /**
     * Gets the associated mailbox.
     *
     * @return The associated mailbox.
     */
    public MailBox getMailBox()
    {
        return this.mailBox;
    }

    /**
     * Sets the associated mailbox.
     *
     * @param mailBox The mailbox to associate.
     */
    public void setMailBox(MailBox mailBox)
    {
        this.mailBox = mailBox;
    }

    /**
     * Gets the security digest.
     *
     * @return The security digest.
     */
    public String getSecurityDigest()
    {
        return this.securityDigest;
    }
}
