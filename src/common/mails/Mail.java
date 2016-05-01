package common.mails;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class Mail
{
    /**
     * 
     */
    protected Map<String, String> headers;
    
    /**
     * 
     */
    protected String body;
    
    /**
     * 
     */
    public Mail()
    {
        this.headers = new HashMap<>();
        this.body = null;
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    public String getHeader(String name)
    {
        return this.headers.getOrDefault(name, null);
    }
    
    /**
     * 
     * @return 
     */
    public Map<String, String> getHeaders()
    {
        return this.headers;
    }
    
    /**
     * 
     * @param header 
     */
    public void addHeader(String header)
    {
        int colonPos = header.indexOf(":");
        
        if(-1 != colonPos)
        {
            this.headers.put(header.substring(0, colonPos), header.substring(colonPos + 2));
        }
        else
        {
            throw new IllegalArgumentException(String.format(
                "Malformed header \"%s\".",
                header
            ));
        }
    }
    
    /**
     * 
     * @param name
     * @param value 
     */
    public void addHeader(String name, String value)
    {
        this.headers.put(name, value);
    }
    
    /**
     * 
     * @return 
     */
    public String getBody()
    {
        return this.body;
    }
    
    /**
     * 
     * @param body 
     */
    public void setBody(String body)
    {
        this.body = body;
    }
    
    /**
     * 
     * @return 
     */
    public int getSize()
    {
        return this.getSize(StandardCharsets.UTF_8);
    }
    
    /**
     * 
     * @param charset
     * @return 
     * @todo
     */
    public int getSize(Charset charset)
    {
        return 0;
    }
    
    /**
     * 
     * @return 
     */
    public byte[] getBytes()
    {
        return this.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * 
     * @param charset
     * @return 
     */
    public byte[] getBytes(Charset charset)
    {
        return new byte[]{};
    }
}
