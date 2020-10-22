import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Main {


    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, ParseException {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String fileNameCSV = "data.csv";

        String fileNameJSON = "data.json";

        String fileNameXML = "data.xml";

        String fileNameJSON2 = "data2.json";


        writeCSV(fileNameCSV);

        parseCSVtoJSON(columnMapping, fileNameCSV, fileNameJSON);

        parseXMLtoJSON(fileNameXML, fileNameJSON2);

        parseJSONbyGSON(fileNameJSON);

        parseJSONbyJackson(fileNameJSON);

    }

    public static List<Employee> jacksonToList(String stringJSON) throws ParseException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

//        CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class, Employee.class);
//        List<Employee> list = mapper.readValue(stringJSON, javaType);

        List<Employee> list = mapper.readValue(stringJSON,
                new TypeReference<List<Employee>>() {
        });

//        JSONParser parser = new JSONParser();
//        JSONArray jsonArray = (JSONArray) parser.parse(stringJSON);
//        for (Object obj : jsonArray) {
//            JSONObject jsonObject = (JSONObject) obj;
//            Employee employee = mapper.readValue(String.valueOf(jsonObject), Employee.class);
//            list.add(employee);
//        }

        return list;
    }

    public static void parseJSONbyJackson(String fileNameJSON) throws ParseException, JsonProcessingException {
        String json = readString(fileNameJSON);
        List<Employee> list = jacksonToList(json);
        System.out.println("Convert Jackson:");
        printEmployees(list);
    }


    public static void parseJSONbyGSON(String fileNameJSON) throws ParseException {
        String json = readString(fileNameJSON);
        List<Employee> list = jsonToList(json);
        System.out.println("Convert GSON:");
        printEmployees(list);
    }

    public static String readString(String fileNameJSON) {
        String line;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileNameJSON))) {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static List<Employee> jsonToList(String stringJSON) throws ParseException {
        List<Employee> list = new ArrayList<>();

        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(stringJSON);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            Employee employee = gson.fromJson(String.valueOf(jsonObject), Employee.class);
            list.add(employee);
        }
        return list;
    }

    public static void printEmployees(List<Employee> list) {
        for (Employee employee : list) {
            System.out.println(employee.toString());
        }
    }

    public static void parseXMLtoJSON(String fileNameXML, String fileNameJSON) throws IOException, SAXException, ParserConfigurationException {
        List<Employee> list = parseXML(fileNameXML);
        String json = listToJson(list);
        writeString(json, fileNameJSON);
    }

    public static void parseCSVtoJSON(String[] columnMapping, String fileNameCSV, String fileNameJSON) {
        List<Employee> list = parseCSV(columnMapping, fileNameCSV);
        String json = listToJson(list);
        writeString(json, fileNameJSON);
    }

    public static List<Employee> parseXML(String fileName) throws ParserConfigurationException, IOException, SAXException {
        List<Employee> list = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileName));

        Node root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);

            if (Node.ELEMENT_NODE == node_.getNodeType()) {
                Element employee = (Element) node_;
                String id = employee.getElementsByTagName("id").item(0).getTextContent();
                String firstName = employee.getElementsByTagName("firstName").item(0).getTextContent();
                String lastName = employee.getElementsByTagName("lastName").item(0).getTextContent();
                String country = employee.getElementsByTagName("country").item(0).getTextContent();
                String age = employee.getElementsByTagName("age").item(0).getTextContent();
                Employee newEmployee = new Employee(Long.parseLong(id), firstName, lastName, country, Integer.parseInt(age));
                list.add(newEmployee);
            }
        }
        return list;
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();

            list = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        String json = gson.toJson(list, listType);
        return json;
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeCSV(String fileNameCSV) {
        String[] employee1 = "1,John,Smith,USA,25".split(",");
        String[] employee2 = "2,Inav,Petrov,RU,23".split(",");
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileNameCSV))) {
            writer.writeNext(employee1);
            writer.writeNext(employee2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

