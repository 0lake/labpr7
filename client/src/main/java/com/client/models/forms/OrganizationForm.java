package com.client.models.forms;


import com.general.exceptions.IncorrectInputInScriptException;
import com.general.exceptions.InvalidFormException;
import com.general.exceptions.MustBeNotEmptyException;
import com.general.exceptions.NotInDeclaredLimitsException;
import com.general.io.Console;
import com.general.io.Interrogator;
import com.general.models.Organization;
import com.general.models.forms.Form;

import java.util.NoSuchElementException;
/**
 * Форма organization.
 */

public class OrganizationForm extends Form<Organization> {
    private final Console console;

    public OrganizationForm(Console console) {
        this.console = console;
    }

    @Override
    public Organization build() throws IncorrectInputInScriptException, InvalidFormException, MustBeNotEmptyException {
        console.println("Введите название, что приведет к созданию новой организации.");
        console.ps2();

        var fileMode = Interrogator.fileMode();
        String input = Interrogator.getUserScanner().nextLine().trim();
        if (fileMode) console.println(input);

        while (input.equals("null") || input.isEmpty()) { // Пока введенное название равно null или пустой строке, запрашиваем правильное значение
            console.printError("Название не может быть пустым! Пожалуйста, введите корректное название:");
            input = Interrogator.getUserScanner().nextLine().trim();
            if (fileMode) console.println(input);
        }

        console.println("! Создание новой организации:");
        try {
            var organization = new Organization(
                    input,
                    input,
                    askYear()
            );
            if (!organization.validate()) throw new InvalidFormException();
            return organization;
        } catch (InvalidFormException | MustBeNotEmptyException exception) {
            console.printError("Неверные данные. Пожалуйста, введите корректные данные.");
            throw exception;
        }
    }

    private int askYear() throws IncorrectInputInScriptException, MustBeNotEmptyException {
        var fileMode = Interrogator.fileMode();
        int year = 0;
        while (true) {
            try {
                console.println("Введите год создания организации:");
                console.ps2();

                String strYear = Interrogator.getUserScanner().nextLine().trim();
                if (fileMode) console.println(strYear);

                if (strYear.isEmpty()) {
                    throw new MustBeNotEmptyException();
                }

                year = Integer.parseInt(strYear);
                if (year <= 0) throw new NotInDeclaredLimitsException();
                if (year >= Integer.MAX_VALUE) {
                    console.printError("Год должен быть меньше максимального значения Integer!");
                    continue;
                }

                break;
            } catch (NoSuchElementException exception) {
                console.printError("Год не распознан!");
                if (fileMode) throw new IncorrectInputInScriptException();
            } catch (NotInDeclaredLimitsException exception) {
                console.printError("Год должен быть больше 0!");
                if (fileMode) throw new IncorrectInputInScriptException();
            } catch (NumberFormatException exception) {
                console.printError("Год должен быть представлен числом в Integer!");
                if (fileMode) throw new IncorrectInputInScriptException();
            } catch (MustBeNotEmptyException exception) {
                console.printError("Год не может быть пустым!");
                if (fileMode) throw new IncorrectInputInScriptException();
            } catch (NullPointerException | IllegalStateException exception) {
                console.printError("Непредвиденная ошибка!");
                System.exit(0);
            }
        }
        return year;
    }

}