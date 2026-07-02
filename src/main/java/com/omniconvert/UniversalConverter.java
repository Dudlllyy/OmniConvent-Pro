package com.omniconvert;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UniversalConverter {


    private final List<String> imageFormats = Arrays.asList(".png", ".jpg", ".jpeg", ".bmp", ".webp", ".ico");
    private final List<String> videoFormats = Arrays.asList(".mp4", ".avi", ".mkv", ".mov");
    private final List<String> audioFormats = Arrays.asList(".mp3", ".wav", ".ogg", ".flac");
    private final List<String> configFormats = Arrays.asList(".json", ".yaml", ".yml");

    public List<String> getAvailableFormats(String inputExt) {
        String ext = inputExt.toLowerCase();
        List<String> available = new ArrayList<>();

        if (imageFormats.contains(ext)) {
            for (String f : imageFormats) {
                if (!f.equals(ext)) available.add(f);
            }
        } else if (videoFormats.contains(ext)) {
            available.addAll(audioFormats);
            available.add(".gif");
        } else if (audioFormats.contains(ext)) {
            for (String f : audioFormats) {
                if (!f.equals(ext)) available.add(f);
            }
        } else if (configFormats.contains(ext)) {
            for (String f : configFormats) {
                if (!f.equals(ext)) available.add(f);
            }
        }

        else if (ext.equals(".gif")) {
            available.addAll(Arrays.asList(".mp4", ".png", ".jpg", ".jpeg"));
        }
        return available;
    }

    public void convert(String inputPath, String outputPath) throws Exception {
        String inputExt = getFileExtension(inputPath).toLowerCase();
        String outputExt = getFileExtension(outputPath).toLowerCase();

        if (imageFormats.contains(inputExt) && imageFormats.contains(outputExt)) {
            convertImage(inputPath, outputPath, outputExt);
        } else if (videoFormats.contains(inputExt) && audioFormats.contains(outputExt)) {
            extractAudioFromVideo(inputPath, outputPath);
        } else if (videoFormats.contains(inputExt) && outputExt.equals(".gif")) {
            convertVideoToGif(inputPath, outputPath);
        } else if (audioFormats.contains(inputExt) && audioFormats.contains(outputExt)) {
            convertAudio(inputPath, outputPath);
        } else if (configFormats.contains(inputExt) && configFormats.contains(outputExt)) {
            convertConfig(inputPath, outputPath, inputExt, outputExt);
        }

        else if (inputExt.equals(".gif") && outputExt.equals(".mp4")) {
            convertGifToVideo(inputPath, outputPath);
        } else if (inputExt.equals(".gif") && Arrays.asList(".png", ".jpg", ".jpeg").contains(outputExt)) {
            convertImage(inputPath, outputPath, outputExt);
        }
        else {
            throw new IllegalArgumentException("Направление конвертации не поддерживается.");
        }
    }

    private void convertImage(String inputPath, String outputPath, String targetExt) throws IOException {
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Не удалось прочитать изображение: " + inputPath);
        }

        String formatName = targetExt.substring(1).toLowerCase();

        boolean requiresSolidBackground = formatName.equals("jpg") ||
                formatName.equals("jpeg") ||
                formatName.equals("bmp");

        if (requiresSolidBackground) {
            BufferedImage newImage = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = newImage.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = newImage;
        } else {
            BufferedImage newImage = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = newImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = newImage;
        }

        boolean success = ImageIO.write(image, formatName, outputFile);
        if (!success) {
            throw new IOException("Критическая ошибка: формат " + formatName + " не поддерживается.");
        }
    }

    private void extractAudioFromVideo(String inputPath, String outputPath) throws IOException, InterruptedException {
        runFFmpegCommand(Arrays.asList("-i", inputPath, "-vn", "-acodec", "libmp3lame", "-y", outputPath));
    }

    // 3. Видео -> GIF
    private void convertVideoToGif(String inputPath, String outputPath) throws IOException, InterruptedException {
        runFFmpegCommand(Arrays.asList(
                "-i", inputPath,
                "-vf", "fps=10,scale=480:-1:flags=lanczos",
                "-y", outputPath
        ));
    }

    // 4. Аудио -> Аудио
    private void convertAudio(String inputPath, String outputPath) throws IOException, InterruptedException {
        runFFmpegCommand(Arrays.asList("-i", inputPath, "-y", outputPath));
    }

    // 5. Конфигурации (JSON <-> YAML)
    private void convertConfig(String inputPath, String outputPath, String inExt, String outExt) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        String content = Files.readString(Paths.get(inputPath));
        Map<String, Object> data;

        if (inExt.equals(".json")) {
            data = jsonMapper.readValue(content, Map.class);
        } else {
            data = yamlMapper.readValue(content, Map.class);
        }

        File outputFile = new File(outputPath);
        if (outExt.equals(".json")) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, data);
        } else {
            yamlMapper.writeValue(outputFile, data);
        }
    }

    // 6. ДОБАВЛЕНО: GIF -> Видео (MP4)
    private void convertGifToVideo(String inputPath, String outputPath) throws IOException, InterruptedException {
        // Добавлен флаг -pix_fmt yuv420p, чтобы полученный MP4 файл без проблем
        // открывался в Telegram, браузерах и стандартных плеерах Windows
        runFFmpegCommand(Arrays.asList("-i", inputPath, "-c:v", "libx264", "-pix_fmt", "yuv420p", "-y", outputPath));
    }

    // Метод запуска FFmpeg (портативный вариант)
    private void runFFmpegCommand(List<String> args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(new File("ffmpeg.exe").getAbsolutePath());
        command.addAll(args);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectError(ProcessBuilder.Redirect.DISCARD);
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg завершился с ошибкой. Код выхода: " + exitCode);
        }
    }

    private String getFileExtension(String path) {
        int lastIndexOf = path.lastIndexOf(".");
        return (lastIndexOf == -1) ? "" : path.substring(lastIndexOf);
    }
}
