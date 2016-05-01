package pop3;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public abstract class Pop3Protocol
{
    /**
     * 
     */
    public static final String RESPONSE_OK = "+OK";
    
    /**
     * 
     */
    public static final String RESPONSE_ERROR = "-ERR";
    
    /**
     * 
     */
    public static final String END_OF_LINE = "\r\n";
    
    /**
     * 
     */
    public static final int DEFAULT_SERVER_PORT = 110;
    
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
