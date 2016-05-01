package pop3.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import pop3.server.commands.AbstractPop3Command;
import pop3.server.commands.ApopCommand;
import pop3.server.commands.DeleteCommand;
import pop3.server.commands.ListCommand;
import pop3.server.commands.PasswordCommand;
import pop3.server.commands.QuitCommand;
import pop3.server.commands.ResetCommand;
import pop3.server.commands.RetrieveCommand;
import pop3.server.commands.StatisticsCommand;
import pop3.server.commands.UserCommand;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class Pop3Server
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
    protected File mailBoxesPath;
    
    /**
     * 
     */
    protected String secret;
    
    /**
     * 
     */
    protected Map<String, AbstractPop3Command> supportedCommands;
    
    /**
     * 
     * @param name
     * @param port
     * @param debug
     * @param mailBoxesPath
     * @param secret 
     */
    public Pop3Server(String name, int port, boolean debug, File mailBoxesPath, String secret)
    {
        // Initialize properties
        this.name = name;
        this.debug = debug;
        this.mailBoxesPath = mailBoxesPath;
        this.secret = secret;
        
        // Register supported commands
        this.supportedCommands = new HashMap<>();
        
        this.supportedCommands.put(
            "QUIT",
            new QuitCommand()
        );
        this.supportedCommands.put(
            "USER",
            new UserCommand()
        );
        this.supportedCommands.put(
            "PASS",
            new PasswordCommand()
        );
        this.supportedCommands.put(
            "APOP",
            new ApopCommand()
        );
        this.supportedCommands.put(
            "STAT",
            new StatisticsCommand()
        );
        this.supportedCommands.put(
            "LIST",
            new ListCommand()
        );
        this.supportedCommands.put(
            "RETR",
            new RetrieveCommand()
        );
        this.supportedCommands.put(
            "DELE",
            new DeleteCommand()
        );
        this.supportedCommands.put(
            "RSET",
            new ResetCommand()
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
            Logger.getLogger(Pop3Server.class.getName()).log(Level.SEVERE, null, ex);
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
                Pop3Connection connection = new Pop3Connection(this, (SSLSocket) this.socket.accept());
                connection.start();
            }
            catch (IOException ex)
            {
                // @todo Log exception better
                Logger.getLogger(Pop3Server.class.getName()).log(Level.SEVERE, null, ex);
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
    public boolean isDebug()
    {
        return this.debug;
    }
    
    /**
     * 
     * @return 
     */
    public File getMailBoxesPath()
    {
        return this.mailBoxesPath;
    }
    
    /**
     * 
     * @return 
     */
    public String getSecret()
    {
        return this.secret;
    }
    
    /**
     * Gets a command if it is supported.
     *
     * @param command The command's name.
     * @return The command, <code>null</code> otherwise.
     */
    public AbstractPop3Command supportsCommand(String command)
    {
        return this.supportedCommands.containsKey(command)
            ? this.supportedCommands.get(command)
            : null;
    }
}
