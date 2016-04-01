package com.home.croaton.audiotravel.domain;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Pair;

import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class RouteSerializer {

    public static final String Route = "route";
    public static final String GeoPoints = "geoPoints";
    public static final String GeoPoint = "geoPoint";
    public static final String PointNumber = "number";
    public static final String PointPosition = "position";
    public static final String AudioPoints = "audioPoints";
    public static final String AudioPoint = "audioPoint";
    public static final String PointRadius = "radius";
    public static final String DoneIndicator = "done";
    public static final String AudioFile = "audio";
    public static final String Id = "id";
    public static final String Name = "name";

    public static void serialize(Route route, FileOutputStream fileOutputStream)
    {
        try
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(Route);
            doc.appendChild(rootElement);

            Element geoPoints = doc.createElement(GeoPoints);
            rootElement.appendChild(geoPoints);

            for(Point geoPoint : route.geoPoints())
            {
                Element pointElement = doc.createElement(GeoPoint);
                pointElement.setAttribute(PointNumber, geoPoint.Number.toString());
                pointElement.setAttribute(PointPosition, geoPointToString(geoPoint));
                geoPoints.appendChild(pointElement);
            }

            Element audioPoints = doc.createElement(AudioPoints);
            rootElement.appendChild(audioPoints);

            for(AudioPoint audioPoint : route.audioPoints())
            {
                Element pointElement = doc.createElement(AudioPoint);
                pointElement.setAttribute(PointNumber, audioPoint.Number.toString());
                pointElement.setAttribute(PointPosition, geoPointToString(audioPoint));
                pointElement.setAttribute(PointRadius, audioPoint.Radius.toString());

                for(String fileName : route.getAudiosForPoint(audioPoint))
                {
                    Element audioResource = doc.createElement(AudioFile);
                    audioResource.setAttribute(Id, fileName.toString());
                    pointElement.appendChild(audioResource);
                }
                audioPoints.appendChild(pointElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(fileOutputStream);

            //StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
        }
        catch (ParserConfigurationException | TransformerException pce)
        {
            pce.printStackTrace();
        }
    }

    public static Route deserializeFromResource(Resources resources, int resourceId)
    {
        InputStream stream = resources.openRawResource(resourceId);

        return deserialize(stream);
    }

    public static Route deserializeFromFile(FileInputStream inputStream)
    {
        return deserialize(inputStream);
    }


    private static Route deserialize(InputStream stream) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;

        try
        {
            builder = docFactory.newDocumentBuilder();
            document = builder.parse(stream);
        }
        catch (ParserConfigurationException | IOException | SAXException e)
        {
            e.printStackTrace();
        }

        NodeList list = document.getElementsByTagName(GeoPoint);
        Route route = new Route();

        for(int i = 0; i < list.getLength(); i++)
        {
            NamedNodeMap attrs = list.item(i).getAttributes();

            route.addGeoPoint(Integer.parseInt(attrs.getNamedItem(PointNumber).getNodeValue()),
                    stringToGeoPoint(attrs.getNamedItem(PointPosition).getNodeValue()));
        }

        list = document.getElementsByTagName(AudioPoint);

        for(int i = 0; i < list.getLength(); i++)
        {
            NamedNodeMap attrs = list.item(i).getAttributes();

            AudioPoint ap = new AudioPoint(Integer.parseInt(attrs.getNamedItem(PointNumber).getNodeValue()),
                    stringToGeoPoint(attrs.getNamedItem(PointPosition).getNodeValue()),
                    Integer.parseInt(attrs.getNamedItem(PointRadius).getNodeValue()));
            route.addAudioPoint(ap);

            NodeList audioFiles = list.item(i).getChildNodes();
            for(int j = 0; j < audioFiles.getLength(); j++)
            {
                NamedNodeMap fileNames = audioFiles.item(j).getAttributes();
                if (fileNames != null)
                    route.addAudioTrack(ap, fileNames.getNamedItem(Id).getNodeValue());
            }
        }

        return route;
    }

    @NonNull
    private static GeoPoint stringToGeoPoint(String serializedLatLng)
    {
        String[] split =  serializedLatLng.split(",");

        return new GeoPoint(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
    }

    @NonNull
    private static String geoPointToString(Point geoPoint)
    {
        return geoPoint.Position.getLatitude() + "," + geoPoint.Position.getLongitude();
    }

    public static HashMap<String, HashMap<Integer, ArrayList<Pair<String, String>>>> deserializeAudioPointNames
            (Resources resources, int resId) {

        InputStream stream = resources.openRawResource(resId);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;

        try
        {
            builder = docFactory.newDocumentBuilder();
            document = builder.parse(stream);
        }
        catch (ParserConfigurationException | IOException | SAXException e)
        {
            e.printStackTrace();
        }

        HashMap<String, HashMap<Integer, ArrayList<Pair<String, String>>>> res = new HashMap<>();
        NodeList audioPoints = document.getElementsByTagName(AudioPoint);
        for(int i = 0; i < audioPoints.getLength(); i++)
        {
            NamedNodeMap audioPointAttrs = audioPoints.item(i).getAttributes();
            int number = Integer.parseInt(audioPointAttrs.getNamedItem(PointNumber).getNodeValue());

            NodeList audios = audioPoints.item(i).getChildNodes();
            for(int j = 0; j < audios.getLength(); j++)
            {
                if (audios.item(j).getLocalName() != AudioFile)
                    continue;

                NamedNodeMap audioFileAttrs = audios.item(j).getAttributes();
                String name = audioFileAttrs.getNamedItem(Name).getNodeValue();

                NodeList languageNames = audios.item(j).getChildNodes();

                for(int k = 0; k < languageNames.getLength(); k++)
                {
                    String lang = languageNames.item(k).getLocalName();
                    if (!res.containsKey(lang))
                        res.put(lang, new HashMap<Integer, ArrayList<Pair<String,String>>>());

                    String audioPointName = languageNames.item(k).getNodeValue();

                    if (res.get(lang).get(number) == null)
                        res.get(lang).put(number, new ArrayList<Pair<String, String>>());

                    res.get(lang).get(number).add(new Pair<>(name, audioPointName));
                }
            }
        }

        return res;
    }
}
