package com.diev.generator;

import com.diev.entity.Participant;
import com.diev.generator.entity.DocumentType;
import com.diev.generator.entity.OutputFormat;
import com.diev.generator.entity.TemplateLayout;
import com.diev.generator.entity.FieldBox;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DocumentGenerator {

    private final TemplateLayout layout;
    private final Font baseFont;
    private final Color textColor;

    public DocumentGenerator(TemplateLayout layout, Path fontPath) throws IOException {
        this(layout, fontPath, new Color(25, 25, 25));
    }

    public DocumentGenerator(TemplateLayout layout, Path fontPath, Color textColor) throws IOException {
        this.layout = layout;
        this.baseFont = loadBaseFont(fontPath);
        this.textColor = textColor;
    }

    public Path generate(
            Path templatePath,
            Participant participant,
            DocumentType documentType,
            OutputFormat outputFormat,
            Path outputDir
    ) throws IOException {

        Files.createDirectories(outputDir);

        BufferedImage templateImage = loadTemplate(templatePath);
        int width = templateImage.getWidth();
        int height = templateImage.getHeight();

        drawText(templateImage, documentType.label(), layout.titleBox().toAbsolute(width, height), baseFont);
        drawText(templateImage, participant.getFullName(), layout.nameBox().toAbsolute(width, height), baseFont);
        drawText(templateImage, participant.getUniversity(), layout.universityBox().toAbsolute(width, height), baseFont);
        drawText(templateImage, participant.getCity(), layout.cityBox().toAbsolute(width, height), baseFont);
        drawText(templateImage, participant.getFaculty(), layout.facultyBox().toAbsolute(width, height), baseFont);
        drawText(templateImage, LocalDate.now().toString(), layout.dateBox().toAbsolute(width, height), baseFont);

        String fileName = buildFileName(documentType, participant.getFullName(), LocalDate.now(), outputFormat);
        Path outputFile = outputDir.resolve(fileName);

        if (outputFormat == OutputFormat.PNG) {
            ImageIO.write(templateImage, "png", outputFile.toFile());
        } else if (outputFormat == OutputFormat.PDF) {
            saveAsPdf(templateImage, outputFile);
        } else {
            throw new IllegalArgumentException("Unsupported output format: " + outputFormat);
        }

        return outputFile;
    }

    private BufferedImage loadTemplate(Path templatePath) throws IOException {
        String lower = templatePath.getFileName().toString().toLowerCase(Locale.ROOT);

        if (lower.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(templatePath.toFile())) {
                PDFRenderer renderer = new PDFRenderer(document);
                // 200 DPI обычно достаточно для аккуратной генерации сертификатов
                return renderer.renderImageWithDPI(0, 200);
            }
        }

        BufferedImage image = ImageIO.read(templatePath.toFile());
        if (image == null) {
            throw new IOException("Cannot read template image: " + templatePath);
        }
        return image;
    }

    private void saveAsPdf(BufferedImage image, Path outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDRectangle pageSize = new PDRectangle(image.getWidth(), image.getHeight());
            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            var pdfImage = LosslessFactory.createFromImage(document, image);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdfImage, 0, 0, image.getWidth(), image.getHeight());
            }

            document.save(outputFile.toFile());
        }
    }

    private void drawText(BufferedImage image, String text, FieldBox box, Font font) {
        Graphics2D g = image.createGraphics();
        try {
            configureGraphics(g);

            Font fittedFont = fitFont(g, text, font, box.width(), box.height(), box.fontSize());
            g.setFont(fittedFont);
            FontMetrics fm = g.getFontMetrics(fittedFont);

            List<String> lines = wrapText(text, fm, box.width());
            int lineHeight = fm.getHeight();
            int totalHeight = lines.size() * lineHeight;

            int startY = box.y() + Math.max(0, (box.height() - totalHeight) / 2) + fm.getAscent();

            for (String line : lines) {
                int lineWidth = fm.stringWidth(line);
                int startX = box.x() + Math.max(0, (box.width() - lineWidth) / 2);
                g.drawString(line, startX, startY);
                startY += lineHeight;
            }
        } finally {
            g.dispose();
        }
    }

    private void configureGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(textColor);
    }

    private Font fitFont(Graphics2D g, String text, Font font, int maxWidth, int maxHeight, int preferredSize) {
        int minSize = 10;
        for (int size = preferredSize; size >= minSize; size--) {
            Font candidate = font.deriveFont((float) size);
            FontMetrics fm = g.getFontMetrics(candidate);
            List<String> lines = wrapText(text, fm, maxWidth);
            int totalHeight = lines.size() * fm.getHeight();

            boolean allLinesFit = true;
            for (String line : lines) {
                if (fm.stringWidth(line) > maxWidth) {
                    allLinesFit = false;
                    break;
                }
            }

            if (allLinesFit && totalHeight <= maxHeight) {
                return candidate;
            }
        }
        return font.deriveFont((float) minSize);
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            result.add("");
            return result;
        }

        for (String paragraph : text.split("\\R")) {
            if (paragraph.isBlank()) {
                result.add("");
                continue;
            }

            String[] words = paragraph.trim().split("\\s+");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (fm.stringWidth(candidate) <= maxWidth) {
                    line = new StringBuilder(candidate);
                } else {
                    if (!line.isEmpty()) {
                        result.add(line.toString());
                    }
                    line = new StringBuilder(word);
                }
            }

            if (!line.isEmpty()) {
                result.add(line.toString());
            }
        }

        return result;
    }

    private Font loadBaseFont(Path fontPath) throws IOException {
        if (fontPath != null && Files.exists(fontPath)) {
            try (InputStream in = Files.newInputStream(fontPath)) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                return font.deriveFont(Font.PLAIN, 24f);
            } catch (FontFormatException e) {
                throw new IOException("Invalid font file: " + fontPath, e);
            }
        }

        // запасной вариант, если файл шрифта не передан
        return new Font("SansSerif", Font.PLAIN, 24);
    }

    private String buildFileName(
            DocumentType documentType,
            String fullName,
            LocalDate date,
            OutputFormat format
    ) {
        String shortName = buildShortName(fullName);
        return sanitize(documentType.label() + "_" + shortName + "_" + date) + "." + format.extension();
    }

    private String buildShortName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Unknown";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return parts[0] + "_" + parts[1];
        }
        return parts[0];
    }

    private String sanitize(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }
}
