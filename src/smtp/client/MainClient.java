package smtp.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public abstract class MainClient
{
    /**
     * Entry point for the SMTP client tests.
     * 
     * @param args Command line arguments.
     * @todo Make a scenario.
     */
    public static void main(String[] args)
    {
        try
        {
            SmtpClient client = new SmtpClient(InetAddress.getByName("127.0.0.1"), 9999, "univ-lyon1.fr");
        }
        catch(UnknownHostException ex)
        {
            Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
