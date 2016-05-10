package common.mails;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import smtp.SmtpProtocol;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 */
public class Mail
{
    /**
     * The pattern to parse a string.
     */
    protected static final Pattern PATTERN_MAIL = Pattern.compile(
        "^((?:(?:[^:\\r\\n]+):(?:[^\\r\\n]+)\\r\\n)+)\\r\\n(.+)\\r\\n\\.\\r\\n",
        Pattern.DOTALL
    );

    /**
     * The mail's headers list.
     */
    protected Map<String, String> headers;

    /**
     * The mail's body.
     */
    protected String body;

    /**
     * Creates a new mail.
     */
    public Mail()
    {
        this.headers = new HashMap<>();
        this.body = null;
    }

    /**
     * Parses a string to build a mail.
     * 
     * @param data The data to parse.
     * @return The newly built mail.
     * @todo Find a way to get rid of the unwanted line breaks in the body.
     */
    public static Mail parse(String data)
    {
        // Initialize vars
        Mail mail = new Mail();
        Matcher matcher = Mail.PATTERN_MAIL.matcher(data);
        
        if(matcher.matches())
        {
            // There are headers and a body
            String[] headersLines = matcher.group(1).split("\r\n");
            
            System.out.print(Arrays.toString(headersLines));
            
            for(String headerLine : headersLines)
            {
                mail.addHeader(headerLine);
            }
            
            // Add body
            mail.setBody(matcher.group(2));
        }
        else
        {
            // There is only the body
            mail.setBody(data.substring(0, data.lastIndexOf(SmtpProtocol.END_OF_DATA)));
        }
        
        return mail;
    }

    /**
     * Gets a header's value from the mail.
     * 
     * @param name The header's name.
     * @return The header's value if it exists, <code>null</code> otherwise.
     */
    public String getHeader(String name)
    {
        return this.headers.getOrDefault(name, null);
    }

    /**
     * Gets every headers from the mail.
     * 
     * @return The headers.
     */
    public Map<String, String> getHeaders()
    {
        return this.headers;
    }

    /**
     * Adds an header to the mail.
     * 
     * @param header A string containing both the header's name and the header's
     * value.
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
     * Adds an header to the mail.
     *
     * @param name The header's name.
     * @param value The header's value.
     */
    public void addHeader(String name, String value)
    {
        this.headers.put(name, value);
    }

    /**
     * Gets the mail's body.
     * 
     * @return The mail's body.
     */
    public String getBody()
    {
        return this.body;
    }

    /**
     * Sets the mail's body.
     * 
     * @param body The mail's body.
     */
    public void setBody(String body)
    {
        this.body = body;
    }

    /**
     * Gets the mail' size using UTF-8 by default.
     * 
     * @return The mail' size.
     */
    public int getSize()
    {
        return this.getSize(StandardCharsets.UTF_8);
    }

    /**
     * Gets the mail' size.
     * 
     * @param charset The charset to use.
     * @return The mail' size.
     */
    public int getSize(Charset charset)
    {
        int size = 0;
        
        // Compute headers' length
        for(Map.Entry<String, String> entry : this.headers.entrySet())
        {
            size += entry.getKey().getBytes(charset).length;
            size += 2; // ": "
            size += entry.getValue().getBytes(charset).length;
            size += 2; // "<CRLF>"
        }

        size += 2; // "<CRLF>"

        // Add the body's length
        size += this.body.getBytes(charset).length;
        
        return size;
    }
}
