package smtp.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import smtp.SmtpProtocol;

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
     */
    public static void main(String[] args)
    {
        try
        {
            SmtpClient client = new SmtpClient(InetAddress.getByName("134.214.118.237"), 9999);

            if (client.ehlo("univ-lyon1.fr") == 1)
            {
                System.out.println("EHLO");

                if (client.mailFrom("thomas.arnaud@univ-lyon1.fr") == 1)
                {
                    System.out.println("MAIL FROM");

                    if (client.rcptTo("bruno.buiret@univ-lyon1.fr") == 1)
                    {
                        System.out.println("RCPT TO");

                        if (client.data() == 2)
                        {
                            System.out.println("DATA");
                            
                            if(client.sendMailBody("Tu es mon ami, même si tu n'es pas très ..." + SmtpProtocol.END_OF_DATA) == 1)
                            {
                                System.out.println("Body");
                            }
                        }
                    }
                }
            }

            if (client.quit() == 3)
            {
                System.out.println("QUIT");
            }
        }
        catch(UnknownHostException ex)
        {
            Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
