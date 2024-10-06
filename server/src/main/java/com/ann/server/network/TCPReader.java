package com.ann.server.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Задача, выполняемая в отдельном потоке, для чтения входящих запросов из канала сокета клиента.
 * Этот класс читает данные из канала, парсит их и передает дальнейшую обработку обработчику.
 * Обеспечивает эффективное неблокирующее чтение с использованием Java NIO.
 */
public class TCPReader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("TCPReader");
    private final SelectionKey key;
    private static final ExecutorService handlerService;

    static {
        handlerService = Executors.newFixedThreadPool(10);
    }

    /**
     * Создает TCPReader с указанным ключом выбора.
     *
     * @param key Ключ выбора, связанный с каналом сокета клиента.
     */
    public TCPReader(SelectionKey key) {
        this.key = key;
    }

    /**
     * Читает данные из канала сокета клиента и передает дальнейшую обработку обработчику.
     * Этот метод является точкой входа для выполнения задачи.
     */
    @Override
    public void run() {
        readRequest();
        // Set interest back to OP_READ after parsing is complete
        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        // Wake up the selector to update interest operations
        key.selector().wakeup();
    }

    /**
     * Читает входящий запрос из канала сокета клиента.
     * Этот метод обрабатывает процесс чтения, обеспечивая неблокирующую работу и обработку частичных чтений.
     */
    private void readRequest() {
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            logger.debug("Чтение запроса от {}", clientSocketChannel.getRemoteAddress());
            int bytesRead;
            while ((bytesRead = clientSocketChannel.read(buffer)) > 0) {
                buffer.flip();
                byteArrayOutputStream.write(buffer.array(), 0, buffer.limit());
                buffer.clear();
            }
            if (bytesRead == -1) {
                // Соединение закрыто клиентом
                closeConnection(clientSocketChannel);
                return;
            }
        } catch (IOException e) {
            logger.error("Ошибка чтения данных: {}", e.getMessage());
            closeConnection(clientSocketChannel);
            return;
        }

        // Запускаем новый обработчик для обработки запроса
        handlerService.submit(new Handler(clientSocketChannel, byteArrayOutputStream));
    }

    /**
     * Закрывает соединение с клиентом.
     * Этот метод обрабатывает закрытие канала и отмену ключа выбора.
     *
     * @param clientSocketChannel Канал сокета клиента, который нужно закрыть.
     */
    private void closeConnection(SocketChannel clientSocketChannel) {
        try {
            key.cancel();
            clientSocketChannel.close();
            logger.info("Соединение закрыто: {}", clientSocketChannel.getRemoteAddress());
        } catch (IOException e) {
            logger.error("Ошибка закрытия канала: {}", e.getMessage());
        }
    }
}
