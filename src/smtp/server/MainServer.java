package smtp.server;

import java.io.File;
import smtp.exceptions.SmtpServerInitializationException;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public abstract class MainServer
{
    /**
     * Entry point for the SMTP server.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        try
        {
            SmtpServer server = new SmtpServer(
                "localhost.fr",
                9999,
                true,
                new File("D:\\")
            );
            server.run();
        }
        catch(SmtpServerInitializationException ex)
        {
        }
    }
}
