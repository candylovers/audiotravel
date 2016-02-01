package com.home.croaton.audiotravel.domain;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class RouteSerializer
{

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

    public static void serialize(Route route)
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
                pointElement.setAttribute(PointPosition, latLngToString(geoPoint));
                geoPoints.appendChild(pointElement);
            }

            Element audioPoints = doc.createElement(AudioPoints);
            rootElement.appendChild(audioPoints);

            for(AudioPoint audioPoint : route.audioPoints())
            {
                Element pointElement = doc.createElement(AudioPoint);
                pointElement.setAttribute(PointNumber, audioPoint.Number.toString());
                pointElement.setAttribute(PointPosition, latLngToString(audioPoint));
                pointElement.setAttribute(PointRadius, audioPoint.Radius.toString());
                pointElement.setAttribute(DoneIndicator, String.valueOf(audioPoint.Done));

                for(Integer audioFileId : route.getAudiosForPoint(audioPoint))
                {
                    Element audioResource = doc.createElement(AudioFile);
                    audioResource.setAttribute(Id, audioFileId.toString());
                    pointElement.appendChild(audioResource);
                }
                audioPoints.appendChild(pointElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            //StreamResult result = new StreamResult(file);

            StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
        }
        catch (ParserConfigurationException | TransformerException pce)
        {
            pce.printStackTrace();
        }
    }

    public static Route deserialize(Resources resources, int resourceId)
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;

        try
        {
            builder = docFactory.newDocumentBuilder();
            document = builder.parse(resources.openRawResource(resourceId));
        }
        catch (ParserConfigurationException | IOException | SAXException e)
        {
            e.printStackTrace();
        }

        if (document == null)
            return null;

        NodeList list = document.getElementsByTagName(GeoPoint);
        Route route = new Route();

        for(int i = 0; i < list.getLength(); i++)
        {
            NamedNodeMap attrs = list.item(i).getAttributes();

            route.addGeoPoint(Integer.parseInt(attrs.getNamedItem(PointNumber).getNodeValue()),
                    stringToLatLng(attrs.getNamedItem(PointPosition).getNodeValue()));
        }

        list = document.getElementsByTagName(AudioPoint);

        for(int i = 0; i < list.getLength(); i++)
        {
            NamedNodeMap attrs = list.item(i).getAttributes();

            AudioPoint ap = new AudioPoint(Integer.parseInt(attrs.getNamedItem(PointNumber).getNodeValue()),
                    stringToLatLng(attrs.getNamedItem(PointPosition).getNodeValue()),
                    Integer.parseInt(attrs.getNamedItem(PointRadius).getNodeValue()));
            route.addAudioPoint(ap);

            NodeList audioFiles = list.item(i).getChildNodes();
            for(int j = 0; j < audioFiles.getLength(); j++)
            {
                NamedNodeMap fileIds = audioFiles.item(j).getAttributes();
                if (fileIds != null)
                    route.addAudioTrack(ap, Integer.parseInt(fileIds.getNamedItem(Id).getNodeValue()));
            }

        }

        return route;
    }

    @NonNull
    private static LatLng stringToLatLng(String serializedLatLng)
    {
        String[] split =  serializedLatLng.split(",");

        return new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
    }

    @NonNull
    private static String latLngToString(Point geoPoint)
    {
        return geoPoint.Position.latitude + "," + geoPoint.Position.longitude;
    }
}