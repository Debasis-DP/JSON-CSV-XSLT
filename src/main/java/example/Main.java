/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.io.StreamUtils;

import org.milyn.payload.StringResult;
import org.xml.sax.InputSource;


import java.io.BufferedWriter;


//XML-JSON
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

// For write operation
import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 *
 * @author Debasis
 */
public class Main {
    private final Smooks smooks;
    static Document document;
    
    protected Main(String smooksFile) throws IOException, SAXException, SmooksException {
        smooks = new Smooks(smooksFile);
    }
    
    protected String runSmooksTransform(ExecutionContext executionContext,byte[] JSON_input) throws IOException,SAXException, SmooksException{
        try{
            StringResult result = new StringResult();
            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(JSON_input)), result);
            return result.toString();
        } finally {
            smooks.close();
        }
        
    }

    
     public static void main(String[] args) throws IOException, SAXException, SmooksException ,Exception{
       
        /*============= JSON to XML============= */
        String intermediateXML_file = "intermediateXML.xml";
        String smooks_file = "smooks-config.xml";
    
        byte[] JSON_input = readInputMessage("inputJSON.jsn");

        System.out.println("_______________________Original JSON file_____________________\n");
        System.out.println(new String(JSON_input));
        System.out.println("______________________________________________________________\n");

        Main mainSmooks = new Main(smooks_file);
        ExecutionContext executionContext = mainSmooks.smooks.createExecutionContext();
        String outXML = mainSmooks.runSmooksTransform(executionContext,JSON_input); 

        String indentedXML = format(outXML);//Indented intermediate XML

        System.out.println("______________________Intermediate XML______________________________\n");
        System.out.println(indentedXML);
        System.out.println("_______________________________________________________________\n");

        try {
             BufferedWriter bufferedWriter_out = new BufferedWriter( new FileWriter (intermediateXML_file));
             bufferedWriter_out.write(indentedXML);
             bufferedWriter_out.close();       
         } catch (IOException e) {
             e.printStackTrace();
         }      

        /*=============== XML to CSV============== */
     
        File stylesheet = new File("XSLT.xsl");
        File xmlSource = new File("intermediateXML.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlSource);

        StreamSource stylesource = new StreamSource(stylesheet);
        Transformer transformer = TransformerFactory.newInstance() .newTransformer(stylesource);

        Source source = new DOMSource(document);
        Result outputTarget = new StreamResult(new File("output.csv"));
        transformer.transform(source, outputTarget);
        System.out.println("Done.");
  }// End main
     

    
    private static byte[] readInputMessage(String JSON_input_file) {
        try {
            return StreamUtils.readStream(new FileInputStream(JSON_input_file));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message />".getBytes();
        }
    }
    
  
    private static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String format(String unformattedXml) {
        try {
            Document document = parseXmlFile(unformattedXml);
 
            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);
 
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
 
    }
}
