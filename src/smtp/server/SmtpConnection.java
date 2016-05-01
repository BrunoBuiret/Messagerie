package smtp.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class SmtpConnection extends Thread
{
    /**
     * 
     */
    protected SmtpServer server;
    
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
    protected SmtpState currentState;
    
    /**
     * 
     */
    protected Set<String> recipientsBuffer;
    
    /**
     * 
     */
    protected StringBuilder bodyBuffer;
    
    /**
     * 
     * @param server
     * @param socket 
     */
    public SmtpConnection(SmtpServer server, SSLSocket socket)
    {
        // Initialize properties
        this.server = server;
        this.socket = socket;
        
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
            Logger.getLogger(SmtpConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     */
    @Override
    public void run()
    {
        String request;
        boolean keepLooping = true;
        
        do
        {
            request = this.readRequest();
            
            if(null != request && !request.isEmpty())
            {
                
            }
            else if(null == request)
            {
                keepLooping = false;
            }
        }
        while(keepLooping);
        
        try {
            this.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(SmtpConnection.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(SmtpConnection.class.getName()).log(
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
            Logger.getLogger(SmtpConnection.class.getName()).log(
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
     * Gets a connection's reference to the server.
     *
     * @return The server.
     */
    public SmtpServer getServer()
    {
        return this.server;
    }
    
    /**
     * Gets a connection's current state.
     *
     * @return The current state.
     */
    public SmtpState getCurrentState()
    {
        return this.currentState;
    }

    /**
     * Sets a connection's current state.
     *
     * @param state The state.
     */
    public void setCurrentState(SmtpState state)
    {
        this.currentState = state;
    }
}
