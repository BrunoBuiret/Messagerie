package pop3;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public abstract class Pop3Protocol
{
    /**
     * The default port of a POP3 server.
     */
    public static final int DEFAULT_SERVER_PORT = 110;
    
    /**
     * A success response must begin with this string.
     */
    public static final String RESPONSE_OK = "+OK";
    
    /**
     * A failure response must begin with this string.
     */
    public static final String RESPONSE_ERROR = "-ERR";
    
    /**
     * Almost every requests and responses must end with these characters.
     */
    public static final String END_OF_LINE = "\r\n";
    
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
