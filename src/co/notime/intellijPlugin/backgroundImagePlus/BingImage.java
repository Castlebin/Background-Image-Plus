package co.notime.intellijPlugin.backgroundImagePlus;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BingImage {
    private static final String USER_HOME = System.getProperty("user.home");
    public static final String DEFAULT_BG_DIR = USER_HOME + File.separator + ".idea_bg";
    private static final String BING_BASE_URL = "https://cn.bing.com";

    public static String download() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());

        File imgDir = new File(DEFAULT_BG_DIR);
        if (!imgDir.exists()) {
            imgDir.mkdir();
            NotificationCenter.notice("Image stored in " + DEFAULT_BG_DIR);
        }

        File imageFile = new File(DEFAULT_BG_DIR, date + "_bing.jpg");
        if (imageFile.exists()) {
            return imageFile.getAbsolutePath();
        }

        try {
            return downloadBingImg(imageFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return imageFile.exists() ? imageFile.getAbsolutePath() : null;
        }
    }

    private static String downloadBingImg(String localFilePath) throws IOException {
        String imgUrl = getBingImgUrl();
        URL url = new URL(imgUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        File newImageFile = new File(localFilePath);
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(newImageFile));

        byte[] buf = new byte[8192];
        int size;
        while ((size = is.read(buf)) != -1) {
            os.write(buf, 0, size);
        }

        is.close();
        os.close();

        return newImageFile.getAbsolutePath();
    }

    private static String getBingImgUrl() throws IOException {
        String bingHomeHtml = getBingHomeHtml();
        return parseImgUrl(bingHomeHtml);
    }

    private static String parseImgUrl(String bingHomeHtml) {
        String tag = "background-image: url(";
        int p1 = bingHomeHtml.indexOf(tag);
        int p2 = bingHomeHtml.indexOf(')', p1);
        String img = bingHomeHtml.substring(p1 + tag.length(), p2);
        return BING_BASE_URL + img;
    }

    private static String getBingHomeHtml() throws IOException {
        URL url = new URL(BING_BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        InputStream inputStream = conn.getInputStream();
        byte[] buf = new byte[8192];
        StringBuilder sb = new StringBuilder();
        int length;
        while ((length = inputStream.read(buf)) != -1) {
            sb.append(new String(buf, 0, length, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

}
