/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.specification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to generating xml files.
 *
 * @author LukeXu
 */
public class XmlGeneratorDemo {

    public XmlGeneratorDemo(ArrayList<String> arrayStrings , boolean xmlForPM) {
        String outputPath = "src/com/specification/temp.xml"; 
        String renameFile;
       if(xmlForPM){
        renameFile = "pminfo.xml";
        generatePMXml(outputPath, arrayStrings);
        changeFileName(outputPath, renameFile);
       }else{
        renameFile = "vminfo.xml";
        generateVMXml(outputPath, arrayStrings);
        changeFileName(outputPath, renameFile);

       }
    }

    private void generatePMXml(String outputPath, ArrayList<String> arrayStrings) {
        try {
            Document doc = generatePMDoc(arrayStrings);
            outputXml(doc, outputPath);
        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }
    }

    private void generateVMXml(String outputPath, ArrayList<String> arrayStrings) {
        try {
            Document doc = generateVMDoc(arrayStrings);
            outputXml(doc, outputPath);
        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }
    }

    /**
     * Output file to specific path
     *
     * @param doc
     * @param fileName
     * @throws Exception
     */
    private void outputXml(Document doc, String fileName) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        //Set the indent number as 4 in XML file
        tf.setAttribute("indent-number", 4);
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(doc);
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
        StreamResult result = new StreamResult(pw);
        transformer.transform(source, result);
        System.out.println("Generating XML file successfully!");
        //Always remember to close the PrinterWriter
        pw.close();
    }

    /**
     * Generating PM XML file
     *
     * @param list
     * @return
     */
    public Document generatePMDoc(ArrayList<String> arrayStrings) {
        Document doc;
        Element root, subRoot = null;
        final int columnNum = 6;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();


        } catch (Exception e) {
            System.out.println("Exception" + e);
            return null;
        }
        root = doc.createElement("PhysicalMachine");
        doc.appendChild(root);
        //Index increment operation is  included in loop
        for (int index = 0; index < arrayStrings.size();) {
            if (index % columnNum == 0) {
                subRoot = doc.createElement("pmInfo");
            }
            generateSubElement(subRoot, doc, "pmType", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "cpu", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "mem", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "storage", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "minPower", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "maxPower", arrayStrings.get(index++));

            if (index % columnNum == 0) {
                root.appendChild(subRoot);
            }
        }
        return doc;
    }

    
        /**
     * Generating VM XML file
     *
     * @param list
     * @return
     */
    public Document generateVMDoc(ArrayList<String> arrayStrings) {
        Document doc;
        Element root, subRoot = null;
        final int columnNum = 5;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();

        } catch (Exception e) {
            System.out.println("Exception" + e);
            return null;
        }
        root = doc.createElement("VirtualMachine");
        doc.appendChild(root);
        //Index increment operation is  included in loop
        for (int index = 0; index < arrayStrings.size();) {
            if (index % columnNum == 0) {
                subRoot = doc.createElement("vmInfo");
            }
            generateSubElement(subRoot, doc, "vmType", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "cpu", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "mem", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "storage", arrayStrings.get(index++));
            generateSubElement(subRoot, doc, "proportion", arrayStrings.get(index++));

            if (index % columnNum == 0) {
                root.appendChild(subRoot);
            }
        }
        return doc;
    }
    
    
    /**
     * Modify the orignal file name
     *
     * @param outputPath
     * @param renameFile
     */
    private void changeFileName(String outputPath, String renameFile) {
        File oldFile = new File(outputPath);
        try {
            if (!oldFile.exists()) {
                oldFile.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("IO Exception" + e);
        }
        //  System.out.println("File name before modifying："+oldFile.getName());
        String rootPath = oldFile.getParent();
        //  System.out.println("The root path ："+rootPath);
        File newFile = new File(rootPath + File.separator + renameFile);
        //  System.out.println(rootPath + File.separator + "xmx.xml");
        //  System.out.println("File name after modifying："+newFile.getName());
        //The dest file can not be existed.
        if (newFile.exists()) {
            System.out.println("Delete File:" + newFile.delete());
        }
        if (oldFile.renameTo(newFile)) {
            System.out.println("Modify Success!");
        } else {
            System.out.println("Modify Failed!");
        }
    }

    /**
     * Refactor method used to generating sub elements.
     *
     * @param root
     * @param doc
     * @param elementNamme
     * @param elementContent
     * @return
     */
    public Element generateSubElement(Element root, Document doc, String elementNamme, String elementContent) {
        Element element = doc.createElement(elementNamme);
        element.setTextContent(elementContent);
        root.appendChild(element);
        return root;
    }
}
