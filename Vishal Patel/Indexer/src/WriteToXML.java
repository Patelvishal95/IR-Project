
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WriteToXML {

   static void Write(TreeMap<String, Integer> tm, String name) {
       name = name.replace(".html", "");
       name=name.concat(".xml");
       try {
           DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
           DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
           // root elements
           Document doc = docBuilder.newDocument();
           Element rootElement = doc.createElement("Index");
           doc.appendChild(rootElement);
           
           // staff elements
           
           
           for (Map.Entry<String, Integer> entry : tm.entrySet()) {
               Element Entry = doc.createElement("Entry");
               rootElement.appendChild(Entry);
               
               Element Term = doc.createElement("Term");
               Term.appendChild(doc.createTextNode(entry.getKey()));
               Entry.appendChild(Term);
               
               Element count = doc.createElement("Count");
               count.appendChild(doc.createTextNode(String.valueOf(entry.getValue())));
               Entry.appendChild(count);
           }
           TransformerFactory transformerFactory = TransformerFactory.newInstance();
           Transformer transformer = transformerFactory.newTransformer();
           DOMSource source = new DOMSource(doc);
           StreamResult result = new StreamResult(new File("./src/index/"+name));
           transformer.transform(source, result);
       } catch (ParserConfigurationException ex) {
           Logger.getLogger(WriteToXML.class.getName()).log(Level.SEVERE, null, ex);
       } catch (TransformerConfigurationException ex) {
           Logger.getLogger(WriteToXML.class.getName()).log(Level.SEVERE, null, ex);
       } catch (TransformerException ex) {
           Logger.getLogger(WriteToXML.class.getName()).log(Level.SEVERE, null, ex);
       }
    }
}
