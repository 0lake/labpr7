package com.ann.server.commands;

import com.general.command.Command;
import com.general.exceptions.CollectionIsEmptyException;
import com.general.exceptions.WrongAmountOfElementsException;
import com.general.managers.CollectionManager;
import com.general.models.Product;
import com.general.network.Request;
import com.general.network.Response;

/**
 * Команда 'sum_of_price'. Сумма значений поля price для всех продуктов.
 */
public class SumOfPrice extends Command {
    private final CollectionManager<Product> collectionManager;

    public SumOfPrice(CollectionManager<Product> collectionManager) {
        super("sum_of_price", "вывести сумму значений поля price для всех элементов коллекции");
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду
     * @return Response с результатом выполнения команды.
     */
    @Override
    public Response execute(Request request) {
        try {
            if (request.getData() != null) {
                throw new WrongAmountOfElementsException();
            }

            int sumOfPrice = getSumOfPrice();
            if (sumOfPrice == 0) {
                throw new CollectionIsEmptyException();
            }

            String resultMessage = "Сумма значений поля price для всех продуктов: " + sumOfPrice;
            return new Response(true, resultMessage);

        } catch (WrongAmountOfElementsException exception) {
            return new Response(false, "Неправильное количество аргументов! Правильное использование: '" + getName() + "'");
        } catch (CollectionIsEmptyException exception) {
            return new Response(false, "Коллекция пуста!");
        }
    }

    private int getSumOfPrice() {
        return collectionManager.getCollection().stream()
                .mapToInt(Product::getPrice)
                .sum();
    }
}
