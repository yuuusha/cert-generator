package com.diev.gui;

import com.diev.entity.Event;
import com.diev.entity.Generation;
import com.diev.entity.Participant;
import com.diev.entity.Template;
import com.diev.generator.entity.DocumentType;
import com.diev.generator.entity.OutputFormat;
import com.diev.service.EventService;
import com.diev.service.GenerationService;
import com.diev.service.ParticipantService;
import com.diev.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class CertificateGeneratorFrame extends JFrame {

    private final EventService eventService;
    private final ParticipantService participantService;
    private final TemplateService templateService;
    private final GenerationService generationService;

    private final JComboBox<ChoiceItem<Event>> eventCombo = new JComboBox<>();
    private final JComboBox<ChoiceItem<Participant>> participantCombo = new JComboBox<>();
    private final JComboBox<ChoiceItem<Template>> templateCombo = new JComboBox<>();
    private final JComboBox<DocumentType> documentTypeCombo = new JComboBox<>(DocumentType.values());
    private final JComboBox<OutputFormat> outputFormatCombo = new JComboBox<>(OutputFormat.values());

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"ID", "Дата", "Мероприятие", "Участник", "Шаблон", "Тип", "Формат", "Статус", "Файл"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public void init() {
        setTitle("Генератор грамот и сертификатов");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 760));
        setLocationRelativeTo(null);

        buildUi();
        refreshAll();
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addEventButton = new JButton("Добавить мероприятие");
        JButton addParticipantButton = new JButton("Добавить участника");
        JButton addTemplateButton = new JButton("Добавить шаблон");
        JButton refreshButton = new JButton("Обновить");

        addEventButton.addActionListener(e -> openEventDialog());
        addParticipantButton.addActionListener(e -> openParticipantDialog());
        addTemplateButton.addActionListener(e -> openTemplateDialog());
        refreshButton.addActionListener(e -> refreshAll());

        actions.add(addEventButton);
        actions.add(addParticipantButton);
        actions.add(addTemplateButton);
        actions.add(refreshButton);

        JPanel generationForm = new JPanel(new GridBagLayout());
        generationForm.setBorder(BorderFactory.createTitledBorder("Генерация документа"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;

        addRow(generationForm, c, "Мероприятие:", eventCombo);
        addRow(generationForm, c, "Участник:", participantCombo);
        addRow(generationForm, c, "Шаблон:", templateCombo);
        addRow(generationForm, c, "Тип документа:", documentTypeCombo);
        addRow(generationForm, c, "Формат:", outputFormatCombo);

        JButton generateButton = new JButton("Сгенерировать");
        generateButton.addActionListener(e -> generateDocument());

        JPanel generatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generatePanel.add(generateButton);

        topPanel.add(actions, BorderLayout.NORTH);
        topPanel.add(generationForm, BorderLayout.CENTER);
        topPanel.add(generatePanel, BorderLayout.SOUTH);

        JTable historyTable = new JTable(historyModel);
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createTitledBorder("История генерации"));

        add(topPanel, BorderLayout.NORTH);
        add(historyScroll, BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, GridBagConstraints c, String label, JComponent field) {
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);

        c.gridy++;
    }

    private void refreshAll() {
        reloadEvents();
        reloadParticipants();
        reloadTemplates();
        refreshHistory();
    }

    private void reloadEvents() {
        reloadCombo(eventCombo, eventService.getAllEvents(), Event::getName, Event::getId);
    }

    private void reloadParticipants() {
        reloadCombo(participantCombo, participantService.getAllParticipants(),
                p -> p.getFullName() + " — " + p.getUniversity(),
                Participant::getId);
    }

    private void reloadTemplates() {
        reloadCombo(templateCombo, templateService.getAllTemplates(),
                t -> t.getName() + " [" + t.getFileType() + "]",
                Template::getId);
    }

    private <T> void reloadCombo(JComboBox<ChoiceItem<T>> combo,
                                 List<T> items,
                                 Function<T, String> labelFn,
                                 Function<T, Long> idFn) {
        combo.removeAllItems();
        for (T item : items) {
            combo.addItem(new ChoiceItem<>(idFn.apply(item), labelFn.apply(item), item));
        }
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    private void generateDocument() {
        ChoiceItem<Event> eventItem = (ChoiceItem<Event>) eventCombo.getSelectedItem();
        ChoiceItem<Participant> participantItem = (ChoiceItem<Participant>) participantCombo.getSelectedItem();
        ChoiceItem<Template> templateItem = (ChoiceItem<Template>) templateCombo.getSelectedItem();
        DocumentType documentType = (DocumentType) documentTypeCombo.getSelectedItem();
        OutputFormat outputFormat = (OutputFormat) outputFormatCombo.getSelectedItem();

        if (eventItem == null || participantItem == null || templateItem == null) {
            JOptionPane.showMessageDialog(this, "Нужно выбрать мероприятие, участника и шаблон.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String filePath = generationService.generateDocument(
                    eventItem.value().getId(),
                    participantItem.value().getId(),
                    templateItem.value().getId(),
                    documentType.name(),
                    outputFormat.name()
            );

            JOptionPane.showMessageDialog(this, "Документ создан:\n" + filePath, "Успех", JOptionPane.INFORMATION_MESSAGE);
            refreshHistory();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);

        Map<Long, String> eventNames = comboToMap(eventCombo);
        Map<Long, String> participantNames = comboToMap(participantCombo);
        Map<Long, String> templateNames = comboToMap(templateCombo);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Generation g : generationService.getGenerationHistory()) {
            String createdAt = g.getCreatedAt() == null
                    ? ""
                    : formatter.format(g.getCreatedAt().atZone(ZoneId.systemDefault()));

            historyModel.addRow(new Object[]{
                    g.getId(),
                    createdAt,
                    eventNames.getOrDefault(g.getEventId(), "#" + g.getEventId()),
                    participantNames.getOrDefault(g.getParticipantId(), "#" + g.getParticipantId()),
                    templateNames.getOrDefault(g.getTemplateId(), "#" + g.getTemplateId()),
                    g.getDocumentType(),
                    g.getOutputFormat(),
                    g.getStatus(),
                    g.getFilePath()
            });
        }
    }

    private <T> Map<Long, String> comboToMap(JComboBox<ChoiceItem<T>> combo) {
        return IntStream.range(0, combo.getItemCount())
                .mapToObj(combo::getItemAt)
                .collect(Collectors.toMap(ChoiceItem::id, ChoiceItem::label, (a, b) -> a));
    }

    private void openEventDialog() {
        EventDialog dialog = new EventDialog(this, eventService, this::reloadEvents);
        dialog.setVisible(true);
    }

    private void openParticipantDialog() {
        ParticipantDialog dialog = new ParticipantDialog(this, participantService, this::reloadParticipants);
        dialog.setVisible(true);
    }

    private void openTemplateDialog() {
        TemplateDialog dialog = new TemplateDialog(this, templateService, this::reloadTemplates);
        dialog.setVisible(true);
    }

    private record ChoiceItem<T>(Long id, String label, T value) {
        @Override
        public String toString() {
            return label;
        }
    }
}