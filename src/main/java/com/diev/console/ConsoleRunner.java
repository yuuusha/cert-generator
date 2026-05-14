package com.diev.console;

import com.diev.entity.Event;
import com.diev.entity.Participant;
import com.diev.entity.Template;
import com.diev.service.EventService;
import com.diev.service.GenerationService;
import com.diev.service.ParticipantService;
import com.diev.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Profile("console")
@Component
@RequiredArgsConstructor
public class ConsoleRunner implements CommandLineRunner {

    private final EventService eventService;
    private final ParticipantService participantService;
    private final TemplateService templateService;
    private final GenerationService generationService;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) {
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createEvent();
                    case "2" -> createParticipant();
                    case "3" -> registerTemplate();
                    case "4" -> generateDocument();
                    case "5" -> listTemplates();
                    case "0" -> {
                        System.out.println("Exit.");
                        return;
                    }
                    default -> System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1 - Create event");
        System.out.println("2 - Create participant");
        System.out.println("3 - Register template from resources/templates");
        System.out.println("4 - Generate document");
        System.out.println("5 - List templates");
        System.out.println("0 - Exit");
        System.out.print("Choose: ");
    }

    private void createEvent() {
        System.out.print("Event name: ");
        String name = scanner.nextLine();

        Event event = eventService.createEvent(name);
        System.out.println("Created event id=" + event.getId() + ", folder=" + event.getFolderPath());
    }

    private void createParticipant() {
        System.out.print("Full name: ");
        String fullName = scanner.nextLine();
        System.out.print("University: ");
        String university = scanner.nextLine();
        System.out.print("City: ");
        String city = scanner.nextLine();
        System.out.print("Faculty: ");
        String faculty = scanner.nextLine();

        Participant participant = new Participant(null, fullName, university, city, faculty);
        Participant saved = participantService.saveParticipant(participant);

        System.out.println("Created participant id=" + saved.getId());
    }

    private void registerTemplate() {
        System.out.print("Template name: ");
        String name = scanner.nextLine();
        System.out.print("File name in resources/templates (for example template1.png): ");
        String fileName = scanner.nextLine();
        System.out.print("File type (PNG/PDF/JPEG/DOCX): ");
        String fileType = scanner.nextLine();

        Template template = templateService.registerTemplateFromResources(name, fileName, fileType);
        System.out.println("Registered template id=" + template.getId());
    }

    private void listTemplates() {
        List<Template> templates = templateService.getAllTemplates();
        if (templates.isEmpty()) {
            System.out.println("No templates found.");
            return;
        }

        for (Template template : templates) {
            System.out.println(
                    "id=" + template.getId() +
                            ", name=" + template.getName() +
                            ", type=" + template.getFileType() +
                            ", path=" + template.getFilePath()
            );
        }
    }

    private void generateDocument() {
        System.out.print("Event id: ");
        Long eventId = Long.parseLong(scanner.nextLine());

        System.out.print("Participant id: ");
        Long participantId = Long.parseLong(scanner.nextLine());

        System.out.print("Template id: ");
        Long templateId = Long.parseLong(scanner.nextLine());

        System.out.print("Document type (CHARTER/CERTIFICATE/DIPLOMA): ");
        String documentType = scanner.nextLine();

        System.out.print("Output format (PDF/PNG): ");
        String outputFormat = scanner.nextLine();

        String result = generationService.generateDocument(
                eventId,
                participantId,
                templateId,
                documentType,
                outputFormat
        );

        System.out.println("Generated file: " + result);
    }
}