package com.ann.server.data;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.general.models.Coordinates;
import com.general.models.Product;
import com.general.models.Organization;
import com.general.models.Product;
import com.general.models.UnitOfMeasure;
import com.ann.server.managers.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс для работы с данными о продуктх в базе данных.
 * Включает методы для создания таблицы, вставки, удаления, обновления и получения данных о продуктх.
 */
public class ProductDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDAO.class);

    // SQL-запрос для создания таблицы "products"
    private static final String CREATE_PRODUCTS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS products (" +
            "id BIGSERIAL PRIMARY KEY, " +
            "name VARCHAR NOT NULL, " +
            "coordinates_x BIGINT NOT NULL, " +
            "coordinates_y FLOAT CHECK (coordinates_y > -519) NOT NULL, " +
            "creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "price INT CHECK (price > 0), " +
            "unitOfMeasure VARCHAR(20) CHECK (unitOfMeasure IN ('METERS', 'GRAMS', 'CENTIMETERS', 'SQUARE_METERS')), " +
            "organization_name VARCHAR NOT NULL, " +
            "organization_year INT CHECK (organization_year IS NULL OR organization_year > 0), " +
            "username VARCHAR(50), " +
            "FOREIGN KEY (username) REFERENCES users(username)" +
            ");";

    // SQL-запрос для получения всех products
    private static final String SELECT_ALL_PRODUCTS_SQL = "SELECT * FROM products";

    // SQL-запрос для вставки нового продукта
    private static final String INSERT_PRODUCT_SQL = "INSERT INTO products (" +
            "name, coordinates_x, coordinates_y, creation_date, price, unitOfMeasure, " +
            "organization_name, organization_year, username) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // SQL-запрос для удаления продукта по ID
    private static final String REMOVE_PRODUCT_SQL = "DELETE FROM products WHERE id = ?";

    // SQL-запрос для обновления данных о продукте
    private static final String UPDATE_PRODUCT_SQL = "UPDATE products SET " +
            "name = ?, coordinates_x = ?, coordinates_y = ?, creation_date = ?, price = ?, " +
            "unitOfMeasure = ?, organization_name = ?, organization_year = ?, " +
            "username = ? " +
            "WHERE id = ?";

    // SQL-запрос для получения product по ID
    private static final String SELECT_PRODUCT_BY_ID_SQL = "SELECT * FROM products WHERE id = ?";

    // SQL-запрос для получения всех products пользователя по имени пользователя
    private static final String SELECT_PRODUCTS_BY_USER_ID_SQL = "SELECT * FROM products WHERE username = ?";

    /**
     * Метод для создания таблицы "products" в базе данных.
     * @param connection Подключение к базе данных
     * @throws SQLException Если возникает ошибка при выполнении SQL-запроса
     */
    public void createProductsTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_PRODUCTS_TABLE_SQL);
        }
    }

    /**
     * Метод для получения всех квартир из базы данных.
     * @param connection Подключение к базе данных
     * @return ResultSet с данными о всех продуктх
     * @throws SQLException Если возникает ошибка при выполнении SQL-запроса
     */
    public ResultSet getAllProducts(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(SELECT_ALL_PRODUCTS_SQL);
    }

    /**
     * Метод для получения всех продуктов как списка объектов Product.
     * @return Список продуктов
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_PRODUCTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Product product = mapResultSetToProduct(resultSet);
                products.add(product);
            }

        } catch (Exception e) {
            // Логируем ошибку и выбрасываем исключение или обрабатываем его соответствующим образом
            LOGGER.error("Ошибка при получении всех продуктов из базы данных", e);
        }

        return products;
    }

    /**
     * Метод для вставки новой квартиры в базу данных.
     * @param product Объект Products с данными о продукте
     * @return ID вставленного продукта или -1 в случае ошибки
     */
    public long insertProduct(Product product) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PRODUCT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // Устанавливаем параметры запроса
            preparedStatement.setString(1, product.getName());
            preparedStatement.setLong(2, product.getCoordinates().getX());
            preparedStatement.setFloat(3, product.getCoordinates().getY());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(product.getCreationDate().atStartOfDay()));
            preparedStatement.setInt(5, product.getPrice());
            preparedStatement.setString(6, product.getUnitOfMeasure().toString());
            preparedStatement.setString(7, product.getOrganization().getName());
            preparedStatement.setInt(8, product.getOrganization().getYear());
            preparedStatement.setString(9, product.getUsername());

            // Выполняем запрос и получаем ID новой записи
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (Exception e) {
            // Логируем ошибку
            LOGGER.error("Ошибка при вставке новой квартиры в базу данных", e);
        }
        return -1;
    }

    /**
     * Метод для удаления продукта по ID.
     * @param id ID продукта
     * @return true, если продукт был успешно удален, иначе false
     * @throws SQLException Если возникает ошибка при выполнении SQL-запроса
     */
    public boolean removeProductById(long id) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(REMOVE_PRODUCT_SQL)) {

            preparedStatement.setLong(1, id);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Метод для обновления данных о продукте.
     * @param product Объект Product с обновленными данными
     * @return true, если данные были успешно обновлены, иначе false
     * @throws SQLException Если возникает ошибка при выполнении SQL-запроса
     */
    public boolean updateProduct(Product product) throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PRODUCT_SQL);
        preparedStatement.setString(1, product.getName());
        preparedStatement.setLong(2, product.getCoordinates().getX());
        preparedStatement.setFloat(3, product.getCoordinates().getY());
        preparedStatement.setTimestamp(4, Timestamp.valueOf(product.getCreationDate().atStartOfDay()));
        preparedStatement.setInt(5, product.getPrice());
        preparedStatement.setString(6, product.getUnitOfMeasure().toString());
        preparedStatement.setString(7, product.getOrganization().getName());
        preparedStatement.setInt(8, product.getOrganization().getYear());
        preparedStatement.setString(9, product.getUsername());
        preparedStatement.setLong(10, product.getId());

        int affectedRows = preparedStatement.executeUpdate();
        return affectedRows > 0;
    }

    // Метод для получения продукта по ID
    public Product getProductById(int id) throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PRODUCT_BY_ID_SQL);
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return mapResultSetToProduct(resultSet);
        }
        return null;
    }

    // Метод для получения всех продукт пользователя
    public ResultSet getProductsByUserId(int userId) throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PRODUCTS_BY_USER_ID_SQL);
        preparedStatement.setInt(1, userId);
        return preparedStatement.executeQuery();
    }

    // Приватный метод для маппинга ResultSet в объект Product
    private Product mapResultSetToProduct(ResultSet resultSet) throws SQLException {
        // Извлекаем данные из ResultSet
        long id = resultSet.getInt("id");
        String name = resultSet.getString("name");

        Long coordinatesX = resultSet.getLong("coordinates_x");
        Float coordinatesY = resultSet.getFloat("coordinates_y");
        Coordinates coordinates = new Coordinates(coordinatesX, coordinatesY);

        LocalDate creationDate = resultSet.getTimestamp("creation_date").toLocalDateTime().toLocalDate();

        Integer price = resultSet.getInt("price");

        String unitOfMeasureString = resultSet.getString("unitOfMeasure");
        UnitOfMeasure unitOfMeasure = unitOfMeasureString != null ? UnitOfMeasure.valueOf(unitOfMeasureString) : null;

        String organizationName = resultSet.getString("organization_name");

        Integer organizationYear = resultSet.getInt("organization_year");
        if (resultSet.wasNull()) {
            organizationYear = null;
        }

        Organization org = new Organization(organizationName, organizationYear);

        String username = resultSet.getString("username");
        if (resultSet.wasNull()) {
            username = null;
        }

        // Создаем объект Product и возвращаем его
        Product product = new Product(name, coordinates, creationDate, price, unitOfMeasure, org);
        product.setUsername(username);
        product.setId(id);  // Если у Product есть метод setId, чтобы установить идентификатор

        return product;
    }

}
