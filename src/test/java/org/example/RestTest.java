package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RestTest {
    private static final Type UNIVERSITY_DATA_TYPE = new TypeToken<List<UniversityData>>() {
    }.getType();

    public static ArrayList<UniversityData> data;

    @BeforeClass
    public static void init() {
        RestAssured.baseURI = "http://universities.hipolabs.com/";
        String filename = Thread.currentThread().getContextClassLoader().getResource("world_universities_and_domains.json").getFile();
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        data = gson.fromJson(reader, UNIVERSITY_DATA_TYPE); // contains the whole reviews list
    }

    private void printDifferenceInUniversitiesList(ArrayList<UniversityData> expectedDataList, ArrayList<UniversityData> actualDataList) {
        if(expectedDataList.size() >= actualDataList.size()) {
            List<UniversityData> differenceList = ((ArrayList<UniversityData>) expectedDataList.clone()).
                    stream().filter(u -> !actualDataList.contains(u)).collect(Collectors.toCollection(ArrayList::new));
            for (UniversityData difference : differenceList) {
                System.out.println("University that was not received by API:");
                System.out.println("    " + difference.name);
                System.out.println("    " + difference.country);
                System.out.println("    " + difference.domains);
            }
        } else {
            List<UniversityData> differenceList = ((ArrayList<UniversityData>) actualDataList.clone()).
                    stream().filter(u -> !actualDataList.contains(u)).collect(Collectors.toCollection(ArrayList::new));
            for (UniversityData difference : differenceList) {
                System.out.println("University that received by API but absent in local storage:");
                System.out.println("    " + difference.name);
                System.out.println("    " + difference.country);
                System.out.println("    " + difference.domains);
            }
        }
    }

    @Test
    public void checkGetUniversitiesByName() throws ParseException {
        System.out.println("Checking GET that returns list of universities by name");
        UniversityData expectedUniversityData = data.get((int) (Math.random() * data.size()));
        String expectedName = "High";
        ArrayList<UniversityData> actualUniversitiesDataList = new ArrayList<>(RestAssured.get("/search?name=" + expectedName)
                .then().statusCode(200)
                .extract().body().jsonPath().getList("", UniversityData.class));
        Assert.assertTrue("API didn't return anything. Considering it as a bug", actualUniversitiesDataList.size() > 0);

        ArrayList<UniversityData> expectedUniversitiesDataList = data.stream().filter(u -> u.name.contains(expectedName)).collect(Collectors.toCollection(ArrayList::new));

        printDifferenceInUniversitiesList(expectedUniversitiesDataList, actualUniversitiesDataList);

        Assert.assertTrue(compareTwoLists(expectedUniversitiesDataList, actualUniversitiesDataList));
    }

    @Test
    public void checkGetUniversitiesByCountry() throws ParseException {
        System.out.println("Checking GET that returns list of universities by country");
        UniversityData expectedUniversitiesData = data.get((int) (Math.random() * data.size()));
        String expectedCountry = expectedUniversitiesData.country;
        System.out.println(expectedCountry);
        ArrayList<UniversityData> actualUniversitiesDataList = new ArrayList<>(RestAssured.get("/search?country=" + expectedCountry)
                .then().statusCode(200)
                .extract().body().jsonPath().getList("", UniversityData.class));
        Assert.assertTrue("API didn't return anything. Considering it as a bug", actualUniversitiesDataList.size() > 0);

        ArrayList<UniversityData> expectedUniversitiesDataList = data.stream().filter(u -> u.country.contains(expectedCountry)).collect(Collectors.toCollection(ArrayList::new));

        printDifferenceInUniversitiesList(expectedUniversitiesDataList, actualUniversitiesDataList);

        Assert.assertTrue(compareTwoLists(expectedUniversitiesDataList, actualUniversitiesDataList));
    }

    @Test
    public void checkGetUniversityByNameAndCountry() throws ParseException {
        System.out.println("Checking GET that returns university by it's name and country");
        UniversityData expectedUniversityData = data.get((int) (Math.random() * data.size()));
        String expectedName = expectedUniversityData.name;
        String expectedCountry = expectedUniversityData.country;
        System.out.println(expectedName);
        System.out.println(expectedCountry);
        ArrayList<UniversityData> actualUniversitiesDataList = new ArrayList<>(RestAssured.get("/search?name=" + expectedUniversityData.name + "&country=" + expectedUniversityData.country).
                then().statusCode(200)
                .extract().body().jsonPath().getList("", UniversityData.class));;
        ArrayList<UniversityData> expectedUniversitiesDataList = data.stream().filter(u -> u.country.contains(expectedCountry) && u.name.contains(expectedName)).collect(Collectors.toCollection(ArrayList::new));

        printDifferenceInUniversitiesList(expectedUniversitiesDataList, actualUniversitiesDataList);

        Assert.assertTrue(compareTwoLists(expectedUniversitiesDataList, actualUniversitiesDataList));

    }

    @Test
    public void checkGetUniversityByDomain() throws ParseException {
        System.out.println("Checking GET that returns university by it's domain");
        String expectedDomain = "highland.edu"; //expectedUniversityData.domains.get(0);
        ArrayList<UniversityData> expectedUniversitiesData = data.stream().filter(u -> u.domains.stream().anyMatch(d -> d.contains(expectedDomain))).collect(Collectors.toCollection(ArrayList::new));//data.get((int) (Math.random() * data.size()));
        System.out.println(expectedDomain);
        ArrayList<UniversityData> actualUniversitiesData = new ArrayList<>(RestAssured.get("/search?domain=" + expectedDomain).
                then().statusCode(200)
                .extract().body().jsonPath().getList("", UniversityData.class));;
        Assert.assertTrue("API didn't return anything. Considering it as a bug", actualUniversitiesData.size() > 0);
        UniversityData actualUniversityData = actualUniversitiesData.get(0);
        Assert.assertNotNull(actualUniversityData.name);
        Assert.assertNotNull(actualUniversityData.country);
        printDifferenceInUniversitiesList(expectedUniversitiesData, actualUniversitiesData);

        Assert.assertTrue(compareTwoLists(expectedUniversitiesData, actualUniversitiesData));
    }

    @Test
    public void checkGetAllDataAvailable() throws ParseException {
        System.out.println("Checking GET that returns all universities by leaving name empty");
        ArrayList<UniversityData> actualUniversitiesDataList = new ArrayList<>(RestAssured.get("/search?name=").
                then().statusCode(200)
                .extract().body().jsonPath().getList("", UniversityData.class));
        Assert.assertTrue("API didn't return anything. This is a potential bug", actualUniversitiesDataList.size() > 0);
        ArrayList<UniversityData> expectedUniversitiesDataList = new ArrayList<>(data);

        printDifferenceInUniversitiesList(expectedUniversitiesDataList, actualUniversitiesDataList);

        Assert.assertTrue(compareTwoLists(expectedUniversitiesDataList, actualUniversitiesDataList));
    }

    public boolean compareTwoLists(List<UniversityData> expectedUniversitiesDataList, List<UniversityData> actualUniversitiesDataList){
        return expectedUniversitiesDataList.containsAll(actualUniversitiesDataList) && actualUniversitiesDataList.containsAll(expectedUniversitiesDataList);
    }
}
