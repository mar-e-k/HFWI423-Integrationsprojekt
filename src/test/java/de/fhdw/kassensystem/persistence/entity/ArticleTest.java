package de.fhdw.kassensystem.persistence.entity;

import de.fhdw.kassensystem.persistence.entity.imported.Article;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für die Entity {@link Article}.
 */
public class ArticleTest {

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Verifiziert, dass articleNumber als Pflichtfeld mit korrekter Länge und
     * Spaltenmapping definiert ist – Grundlage für das Auffinden per Scan/Nummer.
     */
    @Test
    void articleNumber_hasNotNullAndCorrectSizeAndColumnMapping() throws Exception {
        Field field = Article.class.getDeclaredField("articleNumber");

        Size size = field.getAnnotation(Size.class);
        NotNull notNull = field.getAnnotation(NotNull.class);
        Column column = field.getAnnotation(Column.class);

        assertNotNull(size, "@Size muss auf articleNumber vorhanden sein.");
        assertEquals(18, size.max(), "articleNumber darf maximal 18 Zeichen lang sein.");

        assertNotNull(notNull, "@NotNull muss auf articleNumber vorhanden sein.");

        assertNotNull(column, "@Column muss auf articleNumber vorhanden sein.");
        assertEquals("article_number", column.name(), "Spaltenname für articleNumber ist falsch.");
        assertFalse(column.nullable(), "article_number darf nicht nullable sein.");
        assertEquals(18, column.length(), "Länge der Spalte article_number muss 18 sein.");
    }

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Stellt sicher, dass die optionale description sauber begrenzt und korrekt
     * auf die Spalte „description“ gemappt ist.
     */
    @Test
    void description_hasCorrectSizeAndColumnMapping() throws Exception {
        Field field = Article.class.getDeclaredField("description");

        Size size = field.getAnnotation(Size.class);
        Column column = field.getAnnotation(Column.class);

        assertNotNull(size, "@Size muss auf description vorhanden sein.");
        assertEquals(1024, size.max(), "description darf maximal 1024 Zeichen lang sein.");

        assertNotNull(column, "@Column muss auf description vorhanden sein.");
        assertEquals("description", column.name(), "Spaltenname für description ist falsch.");
        assertEquals(1024, column.length(), "Länge der Spalte description muss 1024 sein.");
    }

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Prüft, dass wichtige Textfelder (manufacturer, name, supplier, unit)
     * Pflichtfelder mit einer üblichen Maximal-Länge sind.
     */
    @Test
    void textFields_haveSize255AndAreNotNull() throws Exception {
        assertTextField("manufacturer");
        assertTextField("name");
        assertTextField("supplier");
        assertTextField("unit");
    }

    private void assertTextField(String fieldName) throws Exception {
        Field field = Article.class.getDeclaredField(fieldName);

        Size size = field.getAnnotation(Size.class);
        NotNull notNull = field.getAnnotation(NotNull.class);
        Column column = field.getAnnotation(Column.class);

        assertNotNull(size, "@Size muss auf " + fieldName + " vorhanden sein.");
        assertEquals(255, size.max(), fieldName + " darf maximal 255 Zeichen lang sein.");

        assertNotNull(notNull, "@NotNull muss auf " + fieldName + " vorhanden sein.");

        assertNotNull(column, "@Column muss auf " + fieldName + " vorhanden sein.");
        assertFalse(column.nullable(), fieldName + " darf nicht nullable sein.");
    }

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Verifiziert, dass numerische Felder wie purchasePrice, sellingPrice,
     * stockLevel und taxRatePercent Pflichtfelder mit korrektem Spaltennamen sind.
     */
    @Test
    void numericFields_areNotNullAndMappedCorrectly() throws Exception {
        assertNumericField("purchasePrice", "purchase_price");
        assertNumericField("sellingPrice", "selling_price");
        assertNumericField("stockLevel", "stock_level");
        assertNumericField("taxRatePercent", "tax_rate_percent");
    }

    private void assertNumericField(String fieldName, String columnName) throws Exception {
        Field field = Article.class.getDeclaredField(fieldName);

        NotNull notNull = field.getAnnotation(NotNull.class);
        Column column = field.getAnnotation(Column.class);

        assertNotNull(notNull, "@NotNull muss auf " + fieldName + " vorhanden sein.");
        assertNotNull(column, "@Column muss auf " + fieldName + " vorhanden sein.");
        assertEquals(columnName, column.name(), "Spaltenname für " + fieldName + " ist falsch.");
        assertFalse(column.nullable(), columnName + " darf nicht nullable sein.");
    }

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Stellt sicher, dass isAvailable als Pflichtfeld mit Default „true“ definiert ist
     * und im Java-Objekt sauber initialisiert wird.
     */
    @Test
    void isAvailable_hasColumnDefaultTrueAndNotNullAndJavaDefaultFalse() throws Exception {
        Field field = Article.class.getDeclaredField("isAvailable");

        NotNull notNull = field.getAnnotation(NotNull.class);
        ColumnDefault columnDefault = field.getAnnotation(ColumnDefault.class);
        Column column = field.getAnnotation(Column.class);

        assertNotNull(notNull, "@NotNull muss auf isAvailable vorhanden sein.");
        assertNotNull(column, "@Column muss auf isAvailable vorhanden sein.");
        assertEquals("is_available", column.name(), "Spaltenname für isAvailable ist falsch.");
        assertFalse(column.nullable(), "is_available darf nicht nullable sein.");

        assertNotNull(columnDefault, "@ColumnDefault muss auf isAvailable vorhanden sein.");
        assertEquals("true", columnDefault.value(), "ColumnDefault für is_available muss 'true' sein.");

        Article article = new Article();
        assertEquals(Boolean.FALSE, article.getIsAvailable(),
                "Java-Default von isAvailable sollte false sein.");
    }

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Prüft, dass ein Artikel einen realistischen Datensatz aus den Testdaten
     * (inkl. Name, Preis, Bestände) korrekt über Getter/Setter abbilden kann.
     */
    @Test
    void article_canRepresentSampleRowFromDataSet() {
        Article article = new Article();

        article.setId(61L);
        article.setArticleNumber("A-0061");
        article.setDescription("Standardschrank 19 Zoll, 800x1000mm");
        article.setManufacturer("OptiLink Systems");
        article.setName("Server Rack 42U");
        article.setPurchasePrice(450.0);
        article.setSellingPrice(650.0);
        article.setStockLevel(15);
        article.setSupplier("OptiLink Systems");
        article.setTaxRatePercent(19.0);
        article.setUnit("ST");
        article.setIsAvailable(true);

        assertAll(
                () -> assertEquals(61L, article.getId()),
                () -> assertEquals("A-0061", article.getArticleNumber()),
                () -> assertEquals("Standardschrank 19 Zoll, 800x1000mm", article.getDescription()),
                () -> assertEquals("OptiLink Systems", article.getManufacturer()),
                () -> assertEquals("Server Rack 42U", article.getName()),
                () -> assertEquals(450.0, article.getPurchasePrice()),
                () -> assertEquals(650.0, article.getSellingPrice()),
                () -> assertEquals(15, article.getStockLevel()),
                () -> assertEquals("OptiLink Systems", article.getSupplier()),
                () -> assertEquals(19.0, article.getTaxRatePercent()),
                () -> assertEquals("ST", article.getUnit()),
                () -> assertTrue(article.getIsAvailable())
        );
    }
}