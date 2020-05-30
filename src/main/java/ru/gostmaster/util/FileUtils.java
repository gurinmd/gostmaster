package ru.gostmaster.util;

import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;

/**
 * Класс для работы с файлами.
 * 
 * @author maksimgurin 
 */
public final class FileUtils {

    private FileUtils() { }
    
    /**
     * Получить содержимое файла в виде потока строк.
     * @param fileName имя файла
     * @return строки
     */
    public static Flux<String> fileLines(String fileName) {
        Path path = Paths.get(fileName);
        return Flux.using(() -> Files.lines(path), Flux::fromStream, BaseStream::close);
    }

    /**
     * Получить содержимое файла в массива байт.
     * @param fileName имя файла
     * @return байты
     */
    public static Mono<byte[]> fileContent(String fileName) {
        Path path = Paths.get(fileName);
        return Mono.fromCallable(() -> Files.readAllBytes(path));
    }

    /**
     * Получить имена файлов в директории с необходимым расширением.
     * @param directoryName имя директории
     * @param extension расширение
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
}
