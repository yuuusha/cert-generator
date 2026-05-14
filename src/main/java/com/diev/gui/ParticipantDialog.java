package com.diev.gui;

import com.diev.entity.Participant;
import com.diev.service.ParticipantService;

import javax.swing.*;
import java.awt.*;

public class ParticipantDialog extends JDialog {

    public ParticipantDialog(Frame owner, ParticipantService participantService, Runnable onSaved) {
        super(owner, "Добавить участника", true);

        JTextField fullNameField = new JTextField(30);
        JTextField universityField = new JTextField(30);
        JTextField cityField = new JTextField(30);
        JTextField facultyField = new JTextField(30);

        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");

        saveButton.addActionListener(e -> {
            try {
                Participant participant = new Participant(
                        null,
                        fullNameField.getText().trim(),
                        universityField.getText().trim(),
                        cityField.getText().trim(),
                        facultyField.getText().trim()
                );

                Participant saved = participantService.saveParticipant(participant);

                JOptionPane.showMessageDialog(
                        this,
                        "Участник создан: ID=" + saved.getId(),
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

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("ФИО:"));
        form.add(fullNameField);
        form.add(new JLabel("ВУЗ:"));
        form.add(universityField);
        form.add(new JLabel("Город:"));
        form.add(cityField);
        form.add(new JLabel("Факультет:"));
        form.add(facultyField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        setLayout(new BorderLayout(8, 8));
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }
}
