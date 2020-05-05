package ru.gostmaster.util;

import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.BaseStream;

public class FileUtils {
    public static Flux<String> fileLines(String fileName) {
        Path path = Paths.get(fileName);
        return Flux.using(() -> Files.lines(path), Flux::fromStream, BaseStream::close);
    }
    
    public static Mono<byte[]> fileContent(String fileName) {
        Path path = Paths.get(fileName);
        return Mono.fromCallable(() -> Files.readAllBytes(path));
    }
    
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
