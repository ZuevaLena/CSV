import org.example.Employee;
import org.example.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private static final String TEST_CSV = "test_data.csv";
    private static final String TEST_XML = "test_data.xml";
    private static final String TEST_JSON = "test_output.json";

    @BeforeEach
    void setUp() throws IOException {
        // Создаем тестовые файлы перед каждым тестом
        Files.writeString(Paths.get(TEST_CSV),
                "1,John,Smith,USA,25\n2,Inav,Petrov,RU,23");

        Files.writeString(Paths.get(TEST_XML),
                "<staff><employee><id>1</id><firstName>John</firstName>" +
                        "<lastName>Smith</lastName><country>USA</country><age>25</age></employee>" +
                        "<employee><id>2</id><firstName>Inav</firstName>" +
                        "<lastName>Petrov</lastName><country>RU</country><age>23</age></employee></staff>");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Удаляем тестовые файлы после каждого теста
        Files.deleteIfExists(Paths.get(TEST_CSV));
        Files.deleteIfExists(Paths.get(TEST_XML));
        Files.deleteIfExists(Paths.get(TEST_JSON));
    }

    @Test
    void parseCSV_validFile_returnsCorrectEmployeeList() {
        // given
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        // when
        List<Employee> result = Main.parseCSV(columnMapping, TEST_CSV);

        // then
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(2, result.size(), "Должно быть 2 сотрудника");

        Employee first = result.get(0);
        assertAll("Проверка первого сотрудника",
                () -> assertEquals(1, first.id),
                () -> assertEquals("John", first.firstName),
                () -> assertEquals("Smith", first.lastName),
                () -> assertEquals("USA", first.country),
                () -> assertEquals(25, first.age)
        );
    }

    @Test
    void parseXML_validFile_returnsCorrectEmployeeList() {
        // when
        List<Employee> result = Main.parseXML(TEST_XML);

        // then
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(2, result.size(), "Должно быть 2 сотрудника");

        Employee second = result.get(1);
        assertAll("Проверка второго сотрудника",
                () -> assertEquals(2, second.id),
                () -> assertEquals("Inav", second.firstName),
                () -> assertEquals("Petrov", second.lastName),
                () -> assertEquals("RU", second.country),
                () -> assertEquals(23, second.age)
        );
    }

    @Test
    void listToJson_validList_producesCorrectJson() {
        // given
        List<Employee> list = Arrays.asList(
                new Employee(1, "John", "Smith", "USA", 25),
                new Employee(2, "Inav", "Petrov", "RU", 23)
        );

        // when
        String json = Main.listToJson(list);
        System.out.println("Generated JSON:\n" + json); // Отладочный вывод

        // then
        assertNotNull(json, "JSON не должен быть null");
        assertAll("Проверка ключевых полей в JSON",
                () -> assertTrue(json.replaceAll("\\s", "").contains("\"id\":1"),
                        "Должен содержать id:1"),
                () -> assertTrue(json.contains("\"firstName\":\"John\""),
                        "Должен содержать firstName:John"),
                () -> assertTrue(json.contains("\"country\":\"USA\""),
                        "Должен содержать country:USA")
        );
    }

    @Test
    void integrationTest_csvToJson_createsValidFile() throws IOException {
        // given
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        // when
        List<Employee> list = Main.parseCSV(columnMapping, TEST_CSV);
        String json = Main.listToJson(list);
        Main.writeString(json, TEST_JSON);

        // then
        assertTrue(Files.exists(Paths.get(TEST_JSON)), "Файл должен существовать");

        String fileContent = Files.readString(Paths.get(TEST_JSON));
        System.out.println("File content:\n" + fileContent);

        // Удаляем все пробелы для сравнения
        String compactJson = fileContent.replaceAll("\\s", "");

        assertAll("Проверка содержимого файла",
                () -> assertTrue(compactJson.contains("\"id\":1"),
                        "Должен содержать id:1. Получено: " + fileContent),
                () -> assertTrue(compactJson.contains("\"firstName\":\"John\""),
                        "Должен содержать firstName:John. Получено: " + fileContent),
                () -> assertTrue(compactJson.contains("\"country\":\"USA\""),
                        "Должен содержать country:USA. Получено: " + fileContent)
        );
    }
}