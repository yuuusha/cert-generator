package com.diev.configuration;

import com.diev.generator.DocumentGenerator;
import com.diev.generator.entity.RelativeFieldBox;
import com.diev.generator.entity.TemplateLayout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class DocumentGeneratorConfiguration {

    @Bean
    public TemplateLayout templateLayout() {
        return new TemplateLayout(
                new RelativeFieldBox(0.1, 0.2, 0.8, 0.08, 52),
                new RelativeFieldBox(0.1, 0.4, 0.8, 0.08, 42),
                new RelativeFieldBox(0.1, 0.5, 0.8, 0.07, 34),
                new RelativeFieldBox(0.1, 0.58, 0.8, 0.07, 34),
                new RelativeFieldBox(0.1, 0.66, 0.8, 0.07, 34),
                new RelativeFieldBox(0.7, 0.9, 0.25, 0.05, 26)
        );
    }

    @Bean
    public DocumentGenerator documentGenerator(TemplateLayout layout) throws IOException {
        Path fontPath = Paths.get("src/main/resources/fonts/DejaVuSans.ttf");
        return new DocumentGenerator(layout, fontPath);
    }
}