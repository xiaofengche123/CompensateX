import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 下载 Maven Wrapper JAR 的辅助类。
 */
public class MavenWrapperDownloader {

    /**
     * 主方法：下载 maven-wrapper.jar。
     *
     * @param args 启动参数
     * @throws Exception 下载异常
     */
    public static void main(String[] args) throws Exception {
        File baseDirectory = new File(args.length > 0 ? args[0] : ".");
        File propertiesFile = new File(baseDirectory, ".mvn/wrapper/maven-wrapper.properties");
        String wrapperUrl = readWrapperUrl(propertiesFile);
        File wrapperJar = new File(baseDirectory, ".mvn/wrapper/maven-wrapper.jar");
        downloadFileFromURL(wrapperUrl, wrapperJar);
        System.out.println("Downloaded Maven wrapper jar to " + wrapperJar.getAbsolutePath());
    }

    private static String readWrapperUrl(File propertiesFile) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(propertiesFile.getPath()))) {
            properties.load(in);
        }
        String url = properties.getProperty("wrapperUrl");
        if (url == null || url.trim().isEmpty()) {
            throw new IOException("wrapperUrl not configured in " + propertiesFile.getAbsolutePath());
        }
        return url;
    }

    private static void downloadFileFromURL(String urlString, File destination) throws IOException {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        destination.getParentFile().mkdirs();
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
        }
    }
}
