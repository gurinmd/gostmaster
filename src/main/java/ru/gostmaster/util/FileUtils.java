package ru.gostmaster.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.verification.exception.SignatureUploadException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;

/**
 * Класс для работы с файлами.
 *
 * @author maksimgurin
 */
@Slf4j
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Получить содержимое файла в виде потока строк.
     *
     * @param fileName имя файла
     * @return строки
     */
    public static Flux<String> fileLines(String fileName) {
        Path path = Paths.get(fileName);
        return Flux.using(() -> Files.lines(path), Flux::fromStream, BaseStream::close);
    }

    /**
     * Получить содержимое файла в массива байт.
     *
     * @param fileName имя файла
     * @return байты
     */
    public static Mono<byte[]> fileContent(String fileName) {
        Path path = Paths.get(fileName);
        return Mono.fromCallable(() -> Files.readAllBytes(path));
    }

    /**
     * Получить имена файлов в директории с необходимым расширением.
     *
     * @param directoryName имя директории
     * @param extension     расширение
     * @return список
     */
    public static Flux<String> listFiles(String directoryName, String extension) {
        Path path = Paths.get(directoryName);
        Flux<String> files = Flux.using(() -> Files.list(path), Flux::fromStream, BaseStream::close)
            .map(p -> p.toFile().getAbsolutePath());
        if (StringUtils.hasText(extension)) {
            return files.filter(s -> s.toLowerCase().endsWith(extension.toLowerCase()));
        } else {
            return files;
        }
    }

    /**
     * Получаем тело файла в  виде массива байто.
     *
     * @param dataBufferFlux поток байтовых данных
     * @return byte array
     */
    public static Mono<byte[]> readToBytes(Flux<DataBuffer> dataBufferFlux) {
        Mono<byte[]> res = dataBufferFlux.map(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            return bytes;
        }).collectList().map(bytesList -> {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bytesList.forEach(bytes -> {
                try {
                    stream.write(bytes);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new SignatureUploadException(e);
                }
            });
            return stream.toByteArray();
        });
        return res;
    }
}
