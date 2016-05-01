package smtp.server;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public abstract class MainServer
{
    /**
     * 
     * @param args 
     */
    public static void main(String[] args)
    {
       SmtpServer server = new SmtpServer("univ-lyon1.fr", 9999, true);
       server.run();
    }
}
