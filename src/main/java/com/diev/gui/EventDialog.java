package com.diev.gui;

import com.diev.entity.Event;
import com.diev.service.EventService;

import javax.swing.*;
import java.awt.*;

public class EventDialog extends JDialog {

    public EventDialog(Frame owner, EventService eventService, Runnable onSaved) {
        super(owner, "Добавить мероприятие", true);

        JTextField nameField = new JTextField(30);
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                Event event = eventService.createEvent(name);

                JOptionPane.showMessageDialog(
                        this,
                        "Мероприятие создано: ID=" + event.getId(),
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

        JPanel form = new JPanel(new GridLayout(1, 2, 8, 8));
        form.add(new JLabel("Название мероприятия:"));
        form.add(nameField);

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
