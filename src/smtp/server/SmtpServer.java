package smtp.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import smtp.exceptions.SmtpConnectionInitializationException;
import smtp.exceptions.SmtpServerInitializationException;
import smtp.server.commands.AbstractSmtpCommand;
import smtp.server.commands.DataCommand;
import smtp.server.commands.ExtendedHelloCommand;
import smtp.server.commands.MailCommand;
import smtp.server.commands.QuitCommand;
import smtp.server.commands.RecipientCommand;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class SmtpServer
{
    /**
     * The server' socket.
     */
    protected SSLServerSocket socket;
    
    /**
     * The server's name.
     */
    protected String name;
    
    /**
     * The server's debug mode.
     */
    protected boolean debug;
    
    /**
     * The server' supported commands.
     */
    public Map<String, AbstractSmtpCommand> supportedCommands;
    
    /**
     * Creates a new SMTP server.
     * 
     * @param name The server's name.
     * @param port The server's port.
     * @param debug The server's debug mode.
     * @throws smtp.exceptions.SmtpServerInitializationException If the server
     * can't be properly initialized.
     */
    public SmtpServer(String name, int port, boolean debug)
    throws SmtpServerInitializationException
    {
        // Initialize properties
        this.name = name;
        this.debug = debug;
        
        // Register supported commands
        this.supportedCommands = new HashMap<>();
        
        this.supportedCommands.put(
            "EHLO",
            new ExtendedHelloCommand()
        );
        this.supportedCommands.put(
            "MAIL",
            new MailCommand()
        );
        this.supportedCommands.put(
            "RCPT",
            new RecipientCommand()
        );
        this.supportedCommands.put(
            "DATA",
            new DataCommand()
        );
        this.supportedCommands.put(
            "QUIT",
            new QuitCommand()
        );
        
        // Start server
        try
        {
            // Initialize vars
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            
            // Create socket
            this.socket = (SSLServerSocket) factory.createServerSocket(port);
        }
        catch(IOException ex)
        {
            Logger.getLogger(SmtpServer.class.getName()).log(
                Level.SEVERE,
                "Couldn't start server socket.",
                ex
            );
            
            throw new SmtpServerInitializationException(ex);
        }
    }
    
    /**
     * Launches the server's main loop: accepting new clients and starting their
     * dedicated thread.
     */
    public void run()
    {
        while(true)
        {
            try
            {
                SmtpConnection connection = new SmtpConnection(this, (SSLSocket) this.socket.accept());
                connection.start();
            }
            catch(SmtpConnectionInitializationException ex)
            {
                // Ignore for now
            }
            catch (IOException ex)
            {
                Logger.getLogger(SmtpServer.class.getName()).log(
                    Level.SEVERE,
                    "Couldn't accept new connection.",
                    ex
                );
            }
        }
    }
    
    /**
     * Gets the server's name.
     * 
     * @return The server's name.
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Gets the server's port.
     * 
     * @return The server's port.
     */
    public int getPort()
    {
        return this.socket.getLocalPort();
    }
    
    /**
     * Checks if the server is in debug mode or not.
     * 
     * @return <code>true</code> if the server is in debug mode, <code>false</code>
     * otherwise.
     */
    public boolean isDebug()
    {
        return this.debug;
    }
    
    /**
     * Tests if a command is supported by the server, and, if so, returns it.
     * 
     * @param command The command to test.
     * @return The command if it is supported, <code>null</code> otherwise.
     */
    public AbstractSmtpCommand supportsCommand(String command)
    {
        return this.supportedCommands.containsKey(command)
            ? this.supportedCommands.get(command)
            : null;
    }
}
