package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bruno Buiret (bruno.buiret@etu.univ-lyon1.fr)
 * @author Thomas Arnaud (thomas.arnaud@etu.univ-lyon1.fr)
 * @author Alexis Rabilloud (alexis.rabilloud@etu.univ-lyon1.fr)
 * @see http://stackoverflow.com/questions/190629/what-is-the-easiest-way-to-parse-an-ini-file-in-java#answer-15638381
 */
public class IniFile
{
    /**
     * 
     */
    protected static final Pattern PATTERN_SECTION = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    
    /**
     * 
     */
    protected static final Pattern PATTERN_KEY_VALUE = Pattern.compile("\\s*([^=]*)=(.*)");
    
    /**
     * 
     */
    protected Map<String, Map<String, String>> entries = new HashMap<>();
    
    /**
     * 
     */
    public IniFile()
    {
        
    }
    
    /**
     *
     * @param path
     * @throws java.io.IOException
     */
    public IniFile(String path) throws IOException
    {
        this(new File(path));
    }
    
    /**
     * 
     * @param file 
     * @throws java.io.IOException 
     */
    public IniFile(File file) throws IOException
    {
        this.load(file);
    }
    
    /**
     * 
     * @param path 
     * @throws java.io.IOException 
     */
    public void load(String path) throws IOException
    {
        this.load(new File(path));
    }
    
    /**
     * 
     * @param file 
     * @throws java.io.IOException 
     */
    public void load(File file) throws IOException
    {
        // Perform some check to make sure the file exists and is readable
        if(!file.exists())
        {
            throw new IllegalArgumentException(String.format(
                "File \"%s\" doesn't exist.",
                file.getAbsolutePath()
            ));
        }
        
        if(!file.isFile())
        {
            throw new IllegalArgumentException(String.format(
                "\"%s\" isn't a valid file.",
                file.getAbsolutePath()
            ));
        }
        
        if(!file.canRead())
        {
            throw new IllegalArgumentException(String.format(
                "File \"%s\" can't be read.",
                file.getAbsolutePath()
            ));
        }
        
        // Load everything the file contains
        try(BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            // Initialize vars
            String line;
            String section = null;
            
            // And clean the current map
            this.entries.clear();
            
            while(null != (line = br.readLine()))
            {
                Matcher m = IniFile.PATTERN_SECTION.matcher(line);
                
                // Is it a new section?
                if(m.matches())
                {
                    section = m.group(1).trim();
                }
                // Or are we inside a section?
                else if(null != section)
                {
                    m = IniFile.PATTERN_KEY_VALUE.matcher(line);
                    
                    if(m.matches())
                    {
                        // Initialize vars
                        String key = m.group(1).trim();
                        String value = m.group(2).trim();
                        Map<String, String> sectionMap = this.entries.get(section);
                        
                        // Memorize the new key / value pair
                        if(null == sectionMap)
                        {
                            this.entries.put(section, sectionMap = new HashMap<>());
                        }
                        
                        sectionMap.put(key, value);
                    }
                }
            }
            
            br.close();
        }
    }
    
    /**
     * 
     * @param path 
     * @throws java.io.IOException 
     */
    public void save(String path) throws IOException
    {
        this.save(new File(path));
    }
    
    /**
     * 
     * @param file 
     * @throws java.io.IOException 
     */
    public void save(File file) throws IOException
    {
        // Check the file is writeable
        if(!file.canWrite())
        {
            throw new IllegalArgumentException(String.format(
                "File \"%s\" can't be written.",
                file.getAbsolutePath()
            ));
        }
        
        // Save everything
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file)))
        {
            for(Map.Entry<String, Map<String, String>> sectionEntry : this.entries.entrySet())
            {
                // Write the section header
                bw.write(String.format(
                    "[%s]\r\n",
                    sectionEntry.getKey()
                ));
                
                // Is there key / value pairs to write?
                Map<String, String> sectionMap = this.entries.get(sectionEntry.getKey());
                
                for(Map.Entry<String, String> keyValueEntry : sectionMap.entrySet())
                {
                    bw.write(String.format(
                        "%s=%s\r\n",
                        keyValueEntry.getKey(),
                        keyValueEntry.getValue()
                    ));
                }
            }
            
            bw.close();
        }
    }
    
    /**
     * 
     * @return 
     */
    public Set<String> getSections()
    {
        return this.entries.keySet();
    }
    
    /**
     * 
     * @param section
     * @param key
     * @param defaultValue
     * @return 
     */
    public String getString(String section, String key, String defaultValue)
    {
        Map<String, String> sectionMap = this.entries.get(section);
        
        if(null == sectionMap)
        {
            return defaultValue;
        }
        
        return sectionMap.containsKey(key) ? sectionMap.get(key) : defaultValue;
    }
    
    /**
     * 
     * @param section
     * @param key
     * @param value 
     */
    public void set(String section, String key, String value)
    {
        Map<String, String> sectionMap = this.entries.get(section);
        
        if(null == sectionMap)
        {
            this.entries.put(section, sectionMap = new HashMap<>());
        }
        
        sectionMap.put(key, value);
    }
    
    /**
     * 
     * @param section
     * @param key
     * @param defaultValue
     * @return 
     */
    public int getInt(String section, String key, int defaultValue)
    {
        Map<String, String> sectionMap = this.entries.get(section);
        
        if(null == sectionMap)
        {
            return defaultValue;
        }
        
        return sectionMap.containsKey(key) ? Integer.parseInt(sectionMap.get(key)) : defaultValue;
    }
    
    /**
     * 
     * @param section
     * @param key
     * @param value 
     */
    public void set(String section, String key, int value)
    {
        Map<String, String> sectionMap = this.entries.get(section);
        
        if(null == sectionMap)
        {
            this.entries.put(section, sectionMap = new HashMap<>());
        }
        
        sectionMap.put(key, Integer.toString(value));
    }
    
    /**
     * 
     * @param section
     * @param key
     * @param defaultValue
     * @return 
     */
    public float getFloat(String section, String key, float defaultValue)
    {
        Map<String, String> sectionMap = this.entries.get(section);
        
        if(null == sectionMap)
        {
            return defaultValue;
        }
        
        return sectionMap.containsKey(key) ? Float.parseFloat(sectionMap.get(key)) : defaultValue;
    }
    
    /**
     * 
     * @param section
     * @param key
     * @param value 
     */
    public void set(String section, String key, float value)
    {
        Map<String, String> sectionMap = this.entries.get(section);
        
        if(null == sectionMap)
        {
            this.entries.put(section, sectionMap = new HashMap<>());
        }
        
        sectionMap.put(key, Float.toString(value));
    }
    
    /**
     * 
     * @param section
     * @param key
     * @param defaultValue
     * @return 
     */
    public double getDouble(String section, String key, double defaultValue)
    {
        Map<String, String> sectionMap = this.entries.get(section);
        
        if(null == sectionMap)
        {
            return defaultValue;
        }
        
        return sectionMap.containsKey(key) ? Double.parseDouble(sectionMap.get(key)) : defaultValue;
    }
    
    /**
     * 
     * @param section
     * @param key
     * @param value 
     */
    public void set(String section, String key, double value)
    {
        Map<String, String> sectionMap = this.entries.get(section);
        
        if(null == sectionMap)
        {
            this.entries.put(section, sectionMap = new HashMap<>());
        }
        
        sectionMap.put(key, Double.toString(value));
    }
}
