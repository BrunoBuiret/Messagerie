package smtp;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public abstract class SmtpProtocol
{
    /**
     * 
     */
    public static final int DEFAULT_SERVER_PORT = 110;
    
    /**
     * 
     */
    public static final int DEFAULT_SECURED_SERVER_PORT = 995;
    
    /**
     * 
     */
    public static final String END_OF_LINE = "\r\n";
    
    /**
     * 
     * @param request
     * @return 
     */
    public static String extractCommand(String request)
    {
        return (request.contains(" ") ? request.substring(0, request.indexOf(" ")) : request).trim();
    }
}
