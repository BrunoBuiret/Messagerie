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
import pop3.exceptions.Pop3ConnectionInitializationException;
import pop3.exceptions.Pop3ServerInitializationException;
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
     * The mailboxes' path.
     */
    protected File mailBoxesPath;

    /**
     * The server's secret to use with the <code>APOP</code> command.
     */
    protected String secret;

    /**
     * The commands available to the user.
     */
    protected Map<String, AbstractPop3Command> supportedCommands;

    /**
     * Creates a new POP3 server.
     *
     * @param name The server's name.
     * @param port The server's port.
     * @param debug The server's debug mode.
     * @param mailBoxesPath The mailboxes' path.
     * @param secret The server's secret to use with the <code>APOP</code> command.
     * @throws pop3.exceptions.Pop3ServerInitializationException If the server can't
     * be properly initialized.
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
            Logger.getLogger(Pop3Server.class.getName()).log(
                Level.SEVERE,
                "Couldn't start server socket",
                ex
            );
            
            throw new Pop3ServerInitializationException(ex);
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
                Pop3Connection connection = new Pop3Connection(this, (SSLSocket) this.socket.accept());
                connection.start();
            }
            catch(Pop3ConnectionInitializationException ex)
            {
                // Ignore for now
            }
            catch(IOException ex)
            {
                Logger.getLogger(Pop3Server.class.getName()).log(
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
     * Gets the mailboxes' path.
     *
     * @return The mailboxes' path.
     */
    public File getMailBoxesPath()
    {
        return this.mailBoxesPath;
    }

    /**
     * Gets the server' secret.
     *
     * @return The server' secret.
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
