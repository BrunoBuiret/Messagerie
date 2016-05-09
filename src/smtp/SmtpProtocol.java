package smtp;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public abstract class SmtpProtocol
{
    /**
     * The default port for an unsecured SMTP server.
     */
    public static final int DEFAULT_SERVER_PORT = 110;
    
    /**
     * The default port for a secured SMTP server.
     */
    public static final int DEFAULT_SECURED_SERVER_PORT = 995;
    
    /**
     * Almost every requests and responses must end with these characters.
     */
    public static final String END_OF_LINE = "\r\n";
    
    /**
     * The data sent must end with these characters.
     */
    public static final String END_OF_DATA = "\r\n.\r\n";
    
    /**
     * Extracts the command name from a request.
     * 
     * @param request The request to parse.
     * @return The command name.
     */
    public static String extractCommand(String request)
    {
        return (request.contains(" ") ? request.substring(0, request.indexOf(" ")) : request).trim();
    }
}
