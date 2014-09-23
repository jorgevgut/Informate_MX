package com.codigoprogramacion.informatemx.Helpers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*
 * Created by xymind on 16/09/14.
 */
public class XMLParser {


    public static final String inegi_dataSet = "inegi:DataSet";
    public static final String inegi_series = "inegi:Series";

    public static JSONObject toJSON(InputStream raw_xml) throws Exception{

        JSONObject json = new JSONObject();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        JSONArray data = new JSONArray();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(raw_xml);

        NodeList elements = document.getFirstChild().getChildNodes();
        Node dataSet = null;
        for(int index = 0;index <elements.getLength(); index++){

            if(elements.item(index).getNodeName().equals(inegi_dataSet)){
                Node temp = elements.item(index);
                System.out.println("Found: "+temp.getNodeName());
                NodeList search = temp.getChildNodes().item(1).getChildNodes();
                for (int i = 0;i < search.getLength();i++){
                    if(search.item(i) instanceof Element){
                        //OBS data!
                        dataSet = search.item(i);
                        JSONObject el = new JSONObject();
                        System.out.println(dataSet.getNodeName());
                        el.put("TIME_PERIOD",
                                dataSet.getAttributes().getNamedItem("TIME_PERIOD").getNodeValue());
                        el.put("OBS_VALUE",
                                dataSet.getAttributes().getNamedItem("OBS_VALUE").getNodeValue());

                        data.put(el);
                    }
                }
                dataSet = dataSet.getParentNode();
            }
        }
        json.put("data",data);
        return json;
    }

}
