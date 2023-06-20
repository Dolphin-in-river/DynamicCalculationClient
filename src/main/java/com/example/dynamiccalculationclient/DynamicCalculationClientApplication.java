package com.example.dynamiccalculationclient;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class DynamicCalculationClientApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(DynamicCalculationClientApplication.class, args);
//    }
//
//}

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DynamicCalculationClientApplication {
    public static void main(String[] args) throws Exception {
        String s = "Введите значение (0 для завершения, 1 для отправки сохранения формулы, 2 для получения формулы по id): ";
        System.out.print(s);
        Scanner scanner = new Scanner(System.in);
        short inputType = scanner.nextShort();
        while (inputType != 0) {
            if (inputType != 1 && inputType != 2) {
                System.out.println("Данное значение не поддерживается");
            } else {
                System.out.print("Введите формулу: ");
                String str = scanner.next();
                String formattedSOAPResponse = formatXML(sendRequest(inputType, str));
                System.out.println(formattedSOAPResponse);
            }
            System.out.print(s);
            inputType = scanner.nextShort();
        }
    }


    private static String sendRequest(short type, String parameter) throws Exception {
        if (type == 1) {
            return configureRequest(makeCreateFormulaRequest(parameter));
        }
        return configureRequest(makeGetFormulaRequest(parameter));
    }

    private static String configureRequest(String requestBody) {
        try {
            String responseString = "";
            String outputString = "";
            String wsEndPoint = "http://localhost:8090/ws";
            URL url = new URL(wsEndPoint);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            String xmlInput = requestBody;
            byte[] buffer = xmlInput.getBytes();
            bout.write(buffer);
            byte[] b = bout.toByteArray();
            String SOAPAction = "getUserDetails";
            httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
            httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            httpConn.setRequestProperty("SOAPAction", SOAPAction);
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            try (OutputStream out = httpConn.getOutputStream()) {
                out.write(b);
            }

            try {
                InputStream inputStream;
                if (httpConn.getResponseCode() >= 400) {
                    inputStream = httpConn.getErrorStream();
                } else {
                    inputStream = httpConn.getInputStream();
                }
                InputStreamReader isr = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader in = new BufferedReader(isr);
                while ((responseString = in.readLine()) != null) {
                    outputString = outputString + responseString;
                }
            } catch (IOException e) {
                outputString = "Error: " + e.getMessage();
            }

            return outputString;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static String makeGetFormulaRequest(String parameter) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:gs=\"http://spring.io/guides/gs-producing-web-service\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <gs:getFormulaRequest>\n" +
                "            <gs:id>" + parameter + "</gs:id>\n" +
                "        </gs:getFormulaRequest>" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

    private static String makeCreateFormulaRequest(String parameter) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:gs=\"http://spring.io/guides/gs-producing-web-service\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <gs:createFormulaRequest>\n" +
                "            <gs:formula>" + parameter + "</gs:formula>\n" +
                "        </gs:createFormulaRequest>" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

    private static String formatXML(String unformattedXml) throws Exception {
        try {
            Document document = parseXmlFile(unformattedXml);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 3);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            transformer.transform(source, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private static Document parseXmlFile(String in) throws Exception {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.out.println(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}