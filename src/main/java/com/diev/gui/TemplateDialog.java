package com.diev.gui;

import com.diev.service.TemplateService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

public class TemplateDialog extends JDialog {

    private final JTextField fileNameField = new JTextField(30);

    public TemplateDialog(Frame owner, TemplateService templateService, Runnable onSaved) {
        super(owner, "Добавить шаблон", true);

        JTextField nameField = new JTextField(30);
        JButton chooseButton = new JButton("Выбрать файл");
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");

        chooseButton.addActionListener(e -> chooseTemplateFile());

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String fileName = fileNameField.getText().trim();

                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Введите название шаблона");
                }
                if (fileName.isEmpty()) {
                    throw new IllegalArgumentException("Выберите файл шаблона");
                }

                var template = templateService.registerTemplateFromResources(name, fileName);

                JOptionPane.showMessageDialog(
                        this,
                        "Шаблон зарегистрирован: ID=" + template.getId(),
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                );

                onSaved.run();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());

        JPanel form = new JPanel(new GridLayout(2, 3, 8, 8));
        form.add(new JLabel("Название шаблона:"));
        form.add(nameField);
        form.add(new JLabel());

        form.add(new JLabel("Файл шаблона:"));
        form.add(fileNameField);
        form.add(chooseButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        setLayout(new BorderLayout(8, 8));
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void chooseTemplateFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите файл шаблона");
        chooser.setCurrentDirectory(Paths.get("src/main/resources/templates").toFile());

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            fileNameField.setText(file.getName());
        }
    }
}