package com.client.models.forms;


import com.general.exceptions.IncorrectInputInScriptException;
import com.general.exceptions.InvalidFormException;
import com.general.exceptions.MustBeNotEmptyException;
import com.general.exceptions.NotInDeclaredLimitsException;
import com.general.io.Console;
import com.general.io.Interrogator;
import com.general.models.Coordinates;
import com.general.models.Product;
import com.general.models.Organization;
import com.general.models.UnitOfMeasure;
import com.general.models.forms.Form;

import java.util.NoSuchElementException;

/**
 * Форма продукта.
 */

public class ProductForm extends Form<Product> {
    private final Console console;

    public ProductForm(Console console) {
        this.console = console;
    }

    @Override
    public Product build() throws IncorrectInputInScriptException, InvalidFormException, MustBeNotEmptyException {
        var product = new Product(
                -1,
                askName(),
                askCoordinates(),
                askPrice(),
                askUnitOfMeasure(),
                askOrganization()
        );
        if (!product.validate()) throw new InvalidFormException();
        return product;
    }

    private String askName() throws IncorrectInputInScriptException {
        String name;
        while (true) {
            try {
                console.println("Введите название продукта:");
                console.ps2();

                name = Interrogator.getUserScanner().nextLine().trim();
                if (name.equals("")) throw new MustBeNotEmptyException();
                break;
            } catch (NoSuchElementException exception) {
                console.printError("Название не распознано!");
                throw new IncorrectInputInScriptException();
            } catch (MustBeNotEmptyException exception) {
                console.printError("Название не может быть пустым!");
                // Предлагаем пользователю ввести название продукта заново
                continue;
            } catch (IllegalStateException exception) {
                console.printError("Непредвиденная ошибка!");
                System.exit(0);
            }
        }
        return name;
    }


    private Coordinates askCoordinates() throws IncorrectInputInScriptException, InvalidFormException {
        return new CoordinatesForm(console).build();
    }

    private int askPrice() throws IncorrectInputInScriptException {
        int price;
        final int MIN_PRICE = 1; // Минимальная цена

        while (true) {
            try {
                console.println("Введите цену продукта:");
                console.ps2();

                String input = Interrogator.getUserScanner().nextLine().trim();
                if (input.isEmpty()) {
                    throw new NotInDeclaredLimitsException(); // Если ввод пустой, выбрасываем исключение
                }
                price = Integer.parseInt(input);
                if (price <= 0) throw new NotInDeclaredLimitsException();
                if (price >= Integer.MAX_VALUE) {
                    console.printError("Цена должна быть меньше максимального значения Integer!");
                    continue;
                }
                break;
            } catch (NoSuchElementException exception) {
                console.printError("Цена продукта не распознана! Пожалуйста, введите число.");
            } catch (NotInDeclaredLimitsException exception) {
                console.printError("Цена должна быть больше 0! Пожалуйста, введите корректное значение.");
            } catch (NumberFormatException exception) {
                console.printError("Цена должна быть представлена числом в Integer! Пожалуйста, введите число в Integer.");
            } catch (NullPointerException | IllegalStateException exception) {
                console.printError("Непредвиденная ошибка!");
                System.exit(0);
            }
        }
        return price;
    }



    private UnitOfMeasure askUnitOfMeasure() throws IncorrectInputInScriptException, InvalidFormException {
        return new UnitOfMeasureForm(console).build();
    }

    private Organization askOrganization() throws IncorrectInputInScriptException, InvalidFormException, MustBeNotEmptyException {
        return new OrganizationForm(console).build();
    }

}

