package co.notime.intellijPlugin.backgroundImagePlus;

public class BingImageTest {

    public static void main(String[] args) {
        String path = BingImage.download();
        System.out.println(path);
    }

}