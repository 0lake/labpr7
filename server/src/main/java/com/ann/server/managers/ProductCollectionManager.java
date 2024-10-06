package com.ann.server.managers;

import com.general.managers.CollectionManager;
import com.general.models.Product;
import com.ann.server.data.ProductDAO;
import com.ann.server.data.UserDAO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Класс {@code ProductCollectionManager} управляет коллекцией объектов типа {@link Product}.
 * <p>
 * Этот класс предоставляет методы для загрузки, добавления, удаления и сортировки объектов Product в коллекции.
 * Он работает с базой данных через DAO-классы {@link ProductDAO} и {@link UserDAO}.
 */
public class ProductCollectionManager extends CollectionManager<Product> {
    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final ReentrantLock lock = new ReentrantLock(); // Объект для синхронизации доступа к коллекции

    /**
     * Конструктор, инициализирующий DAO и загружающий коллекцию объектов Product из базы данных.
     *
     * @param productDAO объект для работы с базой данных Product.
     * @param userDAO объект для работы с базой данных пользователей.
     */
    public ProductCollectionManager(ProductDAO productDAO, UserDAO userDAO) {
        this.productDAO = productDAO;
        this.userDAO = userDAO;
        loadCollection();
    }

    /**
     * Создает коллекцию объектов {@link Product}.
     *
     * @return новая коллекция типа {@link PriorityQueue}.
     */
    @Override
    protected Collection<Product> createCollection() {
        return new PriorityQueue<>();
    }

    /**
     * Загружает коллекцию объектов Product из базы данных и сортирует её.
     */
    @Override
    protected void loadCollection() {
        Collection<Product> loadedCollection = productDAO.getAllProducts(); // Загружаем из БД
        lock.lock(); // Начало критической секции
        try {
            setCollection(loadedCollection); // Устанавливаем в коллекцию в памяти
            sortCollection(); // Сортировка коллекции после загрузки
            setLastInitTime(LocalDateTime.now()); // Устанавливаем время инициализации
        } finally {
            lock.unlock(); // Конец критической секции
        }
    }

    /**
     * Возвращает идентификатор объекта Product.
     *
     * @param element объект Product, для которого нужно получить ID.
     * @return ID объекта Product.
     */
    @Override
    protected Long getId(Product element) {
        return element.getId();
    }

    /**
     * Устанавливает новую коллекцию объектов product.
     * Используется блокировка для синхронизации доступа к коллекции.
     *
     * @param collection коллекция объектов product для установки.
     */
    private void setCollection(Collection<Product> collection) {
        lock.lock(); // Начало критической секции
        try {
            super.getCollection().clear(); // Очищаем текущую коллекцию
            super.getCollection().addAll(collection); // Добавляем новые элементы в коллекцию
        } finally {
            lock.unlock(); // Конец критической секции
        }
    }

    /**
     * Добавляет объект Product в коллекцию и базу данных.
     * Используется блокировка для синхронизации доступа к коллекции.
     *
     * @param username имя пользователя, добавляющего объект.
     * @param element  объект Product для добавления.
     * @return ID добавленного объекта или отрицательное значение в случае ошибки.
     */
    @Override
    public Long addToCollection(String username, Product element) {
        element.setUsername(username); // Устанавливаем имя пользователя
        long id = productDAO.insertProduct(element); // Добавляем объект в базу данных
        if (id < 0) return id; // Если ошибка, возвращаем отрицательный ID
        element.setId(id); // Устанавливаем ID объекта

        lock.lock(); // Начало критической секции
        try {
            return super.addToCollection("", element); // Добавляем объект в коллекцию
        } finally {
            lock.unlock(); // Конец критической секции
        }
    }

    /**
     * Удаляет объект Product из коллекции и базы данных.
     * Используется блокировка для синхронизации доступа к коллекции.
     *
     * @param element  объект Product для удаления.
     * @param username имя пользователя, пытающегося удалить объект.
     * @return {@code true}, если удаление прошло успешно, иначе {@code false}.
     */
    @Override
    public boolean removeFromCollection(Product element, String username) {
        if (element == null) return false; // Проверка на null
        if (!element.getUsername().equals(username)) return false; // Проверка, что пользователь является создателем объекта

        try {
            if (!productDAO.removeProductById(element.getId())) throw new Exception(); // Удаление объекта из базы данных
        } catch (Exception e) {
            return false; // В случае ошибки возвращаем false
        }

        lock.lock(); // Начало критической секции
        try {
            return super.removeFromCollection(element, username); // Удаляем объект из коллекции
        } finally {
            lock.unlock(); // Конец критической секции
        }
    }

    /**
     * Сортирует коллекцию объектов Product по имени.
     * Используется блокировка для синхронизации доступа к коллекции.
     */
    @Override
    public void sortCollection() {
        lock.lock(); // Начало критической секции
        try {
            Collection<Product> sortedCollection = getCollection().stream()
                    .sorted(Comparator.comparing(Product::getName)) // Сортировка по имени
                    .collect(Collectors.toList());
            setCollection(sortedCollection); // Устанавливаем отсортированную коллекцию
        } finally {
            lock.unlock(); // Конец критической секции
        }
    }
}
