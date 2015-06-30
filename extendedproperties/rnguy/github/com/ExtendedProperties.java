package extendedproperties.rnguy.github.com;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.Reader;

import java.io.Writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * <p>This is a simple class to expand upon the capabilities of the Java.util.Properties class.
 * It provides for increased functionality mostly surrounding key prefixes and adding support
 * for storing String arrays as properties.</p>
 * <p>Using prefixes to identify data could potentially be handy in storing separate but related
 * items together. An example of which might be database connection details.</p>
 * <ul>
 * <li>connection1.host=localhost</li>
 * <li>connection1.port=1521</li>
 * <li>connection1.service=myDBService</li>
 * <li>connection2.host=10.10.10.1</li>
 * <li>connection2.port=1520</li>
 * <li>connection2.service=myDBService</li>
 * </ul>
 * <p>Calling getProperties() on an ExtendedProperties instance containing the above would yield
 * an array of 2 ExtendedProperties objects; one with connection1 properties and the other with
 * connection2.</p>
 * 
 * @author Aaron Hannah
 * 2015-06-29
 * Last modified: 2015-06-30
 *
 */
public class ExtendedProperties extends Properties {
    private static final long serialVersionUID = 2015063000000000000L;
    private char delimiter = '%';
    private static final char[] SPECIAL_CHARS = {'(','[','{','\\','^','-','=','$','!','|',']','}',')','?','*','+','.'};

    public ExtendedProperties() {
        super();
    }
    
    public ExtendedProperties(Properties properties) {
        super(properties);
    }
    
    /**
     * <p>Prepend the provided prefix to ALL keys currently contained within this ExtendedProperties object.</p>
     * @param prefix - the prefix to prepend to ALL keys.
     */
    public void addPrefix(String prefix) {
        if (prefix == null)
            throw new NullPointerException("Prefix cannot be null");
        
        if (!prefix.matches(".*\\.$"))
            prefix += ".";
        
        for(String key : stringPropertyNames())
            setProperty(prefix + key, (String)remove(key));
    }
    
    /**
     * <p>Add the key/value pairs from a set of properties to this one. Duplicate keys will be overridden with
     * the values in the supplied Properties object.</p>
     * @param properties - The properties to add.
     */
    public void addProperties(Properties properties) {
        addProperties(properties, true);
    }
    
    /**
     * <p>Add the key/value pairs from a set of properties to this one. Duplicate keys will either be overridden
     * with the value from the supplied Properties or skipped, depending on OVERWRITE.</p>
     * @param properties - The properties to add.
     * @param overwrite - Specifies if duplicate keys should be overridden.
     */
    public void addProperties(Properties properties, boolean overwrite) {
        if (properties == null)
            throw new NullPointerException("properties cannot be null");
        
        for(String key : properties.stringPropertyNames())
            if ((containsKey(key) && overwrite) || (!containsKey(key)))
                setProperty(key, properties.getProperty(key));
    }
    
    /**
     * <p>Get the current delimiter being used to delimit array properties.</p>
     * @return The current delimiter.
     */
    public char getDelimiter() {
        return delimiter;
    }
    
    /**
     * <p>Get a list of distinct prefixes found within the ExtendedProperties object.</p>
     * @return Array of prefixes.
     */
    public String[] getPrefixes() {
        ArrayList<String> list = new ArrayList<>();
        for(String key : stringPropertyNames()) {
            String[] prefixes = key.split("\\.");
            if (prefixes.length < 2)
                continue;
            if (!list.contains(prefixes[0]))
                list.add(prefixes[0]);
        }
        return list.toArray(new String[0]);
    }
    
    /**
     * <p>Split this set of ExtendedProperties into an array. The split is made around prefixes so that
     * all items with the same prefix will be grouped into an ExtendedProperties object. Keys without a
     * prefix are combined into 1 ExtendedProperties object.</p>
     * @return Array of ExtendedProperties grouped by prefix.
     */
    public ExtendedProperties[] getProperties() {
        return getProperties(true);
    }
    
    /**
     * <p>Split this set of ExtendedProperties into an array. The split is made around prefixes so that
     * all items with the same prefix will be grouped into an ExtendedProperties object. Keys without a
     * prefix are combined into 1 ExtendedProperties object, unless includeNoPrefix is set to false.</p>
     * @param includeNoPrefix - Include key/value pairs which do not have a prefix.
     * @return Array of ExtendedProperties grouped by prefix.
     */
    public ExtendedProperties[] getProperties(boolean includeNoPrefix) {
        HashMap<String, ExtendedProperties> list = new HashMap<>();
        
        ExtendedProperties noPrefix = new ExtendedProperties();
        for(String key : stringPropertyNames()) {
            if (!key.matches(".*?\\..*")) {
                noPrefix.setProperty(key, (String)get(key));
            } else {
                String prefix = key.split("\\.")[0];
                if (list.get(prefix) == null)
                    list.put(prefix, new ExtendedProperties());
                list.get(prefix).setProperty(key, (String)get(key));
            }
        }
        if (includeNoPrefix)
            list.put(null, noPrefix);
        
        return list.values().toArray(new ExtendedProperties[0]);
    }

    /**
     * <p>Return a new ExtendedProperties object containg the key/value pairs associated with the
     * specified prefix.</p>
     * @param prefix - the prefix that denotes a group of properties.
     * @return ExtendedProperties object with the properties for the specified prefix.
     */
    public ExtendedProperties getPropertiesForPrefix(String prefix) {
        if (prefix == null)
            throw new NullPointerException("Prefix cannot be null");
        
        if (prefix.matches(".*\\.$"))
            prefix = prefix.replaceAll("\\.", "");
        
        ExtendedProperties props = new ExtendedProperties();
        String regex = "^" + prefix + "\\.";
        
        for(String key : stringPropertyNames())
            if (key.matches(regex + ".*"))
                props.setProperty(key.replaceFirst(regex, ""), getProperty(key));
        
        return props;
    }
    
    /**
     * <p>Get the property array associated with the specified key.</p>
     * @param key - the property key.
     * @return the array of values associated with the specified key.
     */
    public String[] getPropertyArray(String key) {
        return getProperty(key).split(Character.toString(delimiter));
    }
    
    /**
     * <p>Change the delimiter to the character provided. Default is '%'. This will update the delimiter in all
     * currently stored properties as well.</p>
     * <p>The following characters cannot be used to delimit a String array: ([{\^-=$!|]})?*+.</p>
     * @param delimiter - The delimiter character to use in separating subsequent arrays.
     * @param replaceAll - Specifies if the current delimiter should be replaced in all values.
     * @return true if delimiter is successfully updated, false otherwise.
     */
    public boolean setDelimiter(char delimiter) {
        validateDelimiter(delimiter);
        
        for(Object value : values())
            if (((String)value).contains(Character.toString(delimiter)))
                return false;
        
        for(String key : stringPropertyNames())
            if (((String)get(key)).contains(Character.toString(this.delimiter)))
                put(key, ((String)get(key)).replaceAll(Character.toString(this.delimiter), Character.toString(delimiter)));
        this.delimiter = delimiter;
        
        return true;
    }
    
    /**
     * <p>Set a property containing an array of values. Use getPropertyArray to retrieve a property into a String array.</p>
     * @param key - the key to be placed into this property list.
     * @param values - the array corresponding to key.
     * @return the previous value (as a String, not an array) of the specified key in this property list, or null, if it did not have one.
     */
    public Object setPropertyArray(String key, String[] values) {
        String array = "";
        
        for(String value : values) {
            if (value.contains(Character.toString(delimiter)))
                throw new IllegalArgumentException("Array of values cannot contain the delimiter: " + delimiter);
            
            array += array.equals("") ? value : delimiter + value;
        }
        
        return setProperty(key, array);
    }
    
    @Override
    public void load(InputStream inStream) throws IOException {
        super.load(inStream);
        extractDelimiter();
    }
    
    @Override
    public void load(Reader reader) throws IOException {
        super.load(reader);
        extractDelimiter();
    }
    
    @Override 
    public void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        super.loadFromXML(in);
        extractDelimiter();
    }
    
    @Override
    public void store(OutputStream out, String comments) throws IOException {
        embedDelimiter();
        super.store(out, comments);
    }
    
    @Override
    public void store(Writer writer, String comments) throws IOException {
        embedDelimiter();
        super.store(writer, comments);
    }
    
    @Override
    public void storeToXML(OutputStream os, String comment) throws IOException {
        embedDelimiter();
        super.storeToXML(os, comment);
    }
    
    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        embedDelimiter();
        super.storeToXML(os, comment, encoding);
    }
    
    private void validateDelimiter(char delimiter) {
        for(char c : SPECIAL_CHARS)
            if (c == delimiter)
                throw new IllegalArgumentException("Delimiter cannot be any of: ([{\\^-=$!|]})?*+.");
    }
    
    private void embedDelimiter() {
        put("ExtendedProperties.delimiter." + serialVersionUID, Character.toString(delimiter));
    }
    
    private void extractDelimiter() throws IOException {
        if (containsKey("ExtendedProperties.delimiter." + serialVersionUID)) {
            try {
                this.delimiter = ((String) get("ExtendedProperties.delimiter." + serialVersionUID)).charAt(0);
                System.out.println(this.delimiter);
            } catch (IndexOutOfBoundsException e) {
                throw new IOException("Failed to read \"ExtendedProperties.delimiter." + serialVersionUID + "\" value.");
            }
            remove("ExtendedProperties.delimiter." + serialVersionUID);
        }
    }
}
