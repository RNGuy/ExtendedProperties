# ExtendedProperties

<p>This is a simple class to expand upon the capabilities of the Java.util.Properties class.
 It provides for increased functionality mostly surrounding key prefixes and adding support
 for storing String arrays as properties.</p>
 <p>Using prefixes to identify data could potentially be handy in storing separate but related
 items together. An example of which might be database connection details.</p>
 <ul>
 <li>connection1.host=localhost</li>
 <li>connection1.port=1521</li>
 <li>connection1.service=myDBService</li>
 <li>connection2.host=10.10.10.1</li>
 <li>connection2.port=1520</li>
 <li>connection2.service=myDBService</li>
 </ul>
 <p>Calling getProperties() on an ExtendedProperties instance containing the above would yield
 an array of 2 ExtendedProperties objects; one with connection1 properties and the other with
 connection2.</p></div>

# Author
Aaron Hannah, 2015

# Note
This class has only undergone minimal testing at this point.
