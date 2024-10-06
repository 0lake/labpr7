package com.client.models.forms;

import com.general.exceptions.IncorrectInputInScriptException;
import com.general.io.Console;
import com.general.io.Interrogator;
import com.general.models.UnitOfMeasure;
import com.general.models.forms.Form;

import java.util.NoSuchElementException;

/**
 * Форма вида.
 */
public class UnitOfMeasureForm extends Form<UnitOfMeasure> {
    private final Console console;

    public UnitOfMeasureForm(Console console) {
        this.console = console;
    }

    @Override
    public UnitOfMeasure build() throws IncorrectInputInScriptException {
        var fileMode = Interrogator.fileMode();

        String strView;
        UnitOfMeasure unitOfMeasure;
        while (true) {
            try {
                console.println("Список видов - " + UnitOfMeasure.names());
                console.println("Введите вид:");
                console.ps2();

                strView = Interrogator.getUserScanner().nextLine().trim();
                if (fileMode) console.println(strView);

                unitOfMeasure = UnitOfMeasure.valueOf(strView.toUpperCase());
                break;
            } catch (NoSuchElementException exception) {
                console.printError("Вид не распознан!");
                if (fileMode) throw new IncorrectInputInScriptException();
            } catch (IllegalArgumentException exception) {
                console.printError("Такого вида нет в списке!");
                if (fileMode) throw new IncorrectInputInScriptException();
            } catch (IllegalStateException exception) {
                console.printError("Непредвиденная ошибка!");
                System.exit(0);
            }
        }
        return unitOfMeasure;
    }
}
