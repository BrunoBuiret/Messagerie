package smtp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

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
     * @param host
     * @param port 
     */
    public SmtpClient(InetAddress host, int port)
    {
        try
        {
            // Initialize vars
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            
            System.out.println(Arrays.toString(factory.getSupportedCipherSuites()));
            
            // Create socket
            this.socket = (SSLSocket) factory.createSocket(host, port);
            
            // Determine which cipher suites can be used
            this.socket.setEnabledCipherSuites(this.socket.getSupportedCipherSuites());
            
            // Start handshake
            this.socket.startHandshake();
            
            // Get streams
            this.socketWriter = new BufferedOutputStream(this.socket.getOutputStream());
            this.socketReader = new BufferedInputStream(this.socket.getInputStream());
            
            // **
            this.sendRequest("Essai SSL\r\n");
            
            this.socket.close();
        }
        catch(IOException ex)
        {
            // @todo Throw exception to avoid methods being executed.
            Logger.getLogger(SmtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @return 
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

                if(-1 != readByte)
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
     * 
     * @param request 
     * @throws java.io.IOException 
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
            System.out.println("before flush()");
            this.socketWriter.flush();
            System.out.println("after flush()");
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
}
