package co.notime.intellijPlugin.backgroundImagePlus;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BingImage {
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String DEFAULT_BG_DIR = USER_HOME + File.separator + ".idea_bg";

    public static final long ONE_HOUR_BY_MILLS = 60 * 60 * 1000;
    public static final long ONE_DAY_BY_MILLS = 24 * ONE_HOUR_BY_MILLS;
    public static final long INTERVAL_TIME = ONE_DAY_BY_MILLS;

    public static String download() {
        File imageFile = new File(DEFAULT_BG_DIR, "bing.jpg");
        File imgDir = new File(DEFAULT_BG_DIR);
        if (!imgDir.exists()) {
            imgDir.mkdir();
            NotificationCenter.notice("Image stored in " + DEFAULT_BG_DIR);
        }

        try {
            if (imageFile.exists()
                    && System.currentTimeMillis() - Files.getLastModifiedTime(imageFile.toPath()).toMillis() < INTERVAL_TIME) {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL("https://cn.bing.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream inputStream = conn.getInputStream();
            byte[] buf = new byte[8192];
            StringBuilder sb = new StringBuilder();

            String text;
            int length;
            while ((length = inputStream.read(buf)) != -1) {
                text = new String(buf, 0, length, StandardCharsets.UTF_8);
                sb.append(text);
            }

            text = sb.toString();
            int p1 = text.indexOf("background-image: url(");
            int p2 = text.indexOf(41, p1);
            String img = text.substring(p1 + 22, p2);
            String imgUrl = "https://cn.bing.com" + img;
            conn = (HttpURLConnection) (new URL(imgUrl)).openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            File newImageFile = new File(DEFAULT_BG_DIR, System.currentTimeMillis() + "_bing.jpg");
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(newImageFile));

            int size;
            while ((size = is.read(buf)) != -1) {
                os.write(buf, 0, size);
            }

            is.close();
            os.close();

            try {
                if (imageFile.exists()
                        && md5(newImageFile).equals(md5(imageFile))) {
                    Files.delete(newImageFile.toPath());
                    return null;
                } else {
                    Files.copy(newImageFile.toPath(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return newImageFile.getAbsolutePath();
                }
            } catch (Exception e) {
                Files.delete(newImageFile.toPath());
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return imageFile.exists() ? imageFile.getAbsolutePath() : null;
        }
    }

    public static String md5(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }

            byte[] byteArray = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : byteArray) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
