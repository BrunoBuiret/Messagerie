package smtp.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
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
     * 
     */
    protected SSLServerSocket socket;
    
    /**
     * 
     */
    protected String name;
    
    /**
     * 
     */
    protected boolean debug;
    
    /**
     * 
     */
    public Map<String, AbstractSmtpCommand> supportedCommands;
    
    /**
     * 
     * @param name
     * @param port
     * @param debug 
     */
    public SmtpServer(String name, int port, boolean debug)
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
            // @todo Throw exception to avoid {@code #run()} being executed.
            Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
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
            catch (IOException ex)
            {
                // @todo Log exception better
                Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * 
     * @return 
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * 
     * @return 
     */
    public int getPort()
    {
        return this.socket.getLocalPort();
    }
    
    /**
     * 
     * @return 
     */
    public boolean isDebug()
    {
        return this.debug;
    }
    
    /**
     * 
     * @param command
     * @return 
     */
    public AbstractSmtpCommand supportsCommand(String command)
    {
        return this.supportedCommands.containsKey(command)
            ? this.supportedCommands.get(command)
            : null;
    }
}
