package tests;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;


public class FilesTests {
    ClassLoader classLoader = FilesTests.class.getClassLoader();

    @Test
    @DisplayName("Проверка чтения файлов из zip-архива")
    void zipTest() throws Exception {
        InputStream is = classLoader.getResourceAsStream("zipExample.zip");
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().contains("pdf")) {
                PDF pdf = new PDF(zis);
                assertThat(pdf.text).contains("This is a small demonstration .pdf file");
            } else if (entry.getName().contains("xls")) {
                XLS xls = new XLS(zis);
                assertThat((
                        xls.excel.getSheetAt(0)
                                .getRow(53)
                                .getCell(1)
                                .getStringCellValue()
                        ).contains("Philip") && (
                         xls.excel.getSheetAt(0)
                                        .getRow(53)
                                        .getCell(4)
                                        .getStringCellValue()
                        ).contains("France")
                );
            } else if (entry.getName().contains("csv")) {
                CSVReader csvReader = new CSVReader(new InputStreamReader(zis, UTF_8));
                List<String[]> csv = csvReader.readAll();
                assertThat(csv).contains(
                        new String[] {
                                "craig@example.com",
                                "4081",
                                "30no86",
                                "cj4081",
                                "Craig",
                                "Johnson",
                                "Depot",
                                "London"
                        },
                        new String[] {
                                "jamie@example.com",
                                "5079",
                                "09ja61",
                                "js5079",
                                "Jamie",
                                "Smith",
                                "Engineering",
                                "Manchester"
                        }
                        );
            }
        }
    }

    @Test
    @DisplayName("Проверка чтения json")
    void jsonTest() throws Exception {
        InputStream is = classLoader.getResourceAsStream("jsonExample.json");
        ObjectMapper mapper = new ObjectMapper();
        assert is != null;
        JsonNode jsonNode = mapper.readTree(new InputStreamReader(is));
        assertThat(jsonNode.findValue("result").asBoolean()).isEqualTo(true);
        assertThat(jsonNode.findValue("name").asText())
                .isEqualTo("Кондиционер (сплит-система) Dantex RK-24 SFM настенный");
        int i = 0;
        while (i < jsonNode.size()) {
            if (jsonNode.findValue("offers").get(i)
                    .findValue("priceCurrency").asText().equals("RUB"))
            {
                assertThat(jsonNode.findValue("offers").get(i)
                        .findValue("price").asInt()).isEqualTo(79999);
            } else if (jsonNode.findValue("offers").get(i)
                    .findValue("priceCurrency").asText().equals("KZT"))
            {
                assertThat(jsonNode.findValue("offers").get(i)
                        .findValue("price").asInt()).isEqualTo(688759);
            }
            i++;
        }
    }
}
