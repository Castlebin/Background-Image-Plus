package co.notime.intellijPlugin.backgroundImagePlus;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import java.io.File;

public class RandomBackgroundTask implements Runnable {
    private ImagesHandler imagesHandler = new ImagesHandler();

    public void run() {
        PropertiesComponent prop = PropertiesComponent.getInstance();
        String folder = prop.getValue("BackgroundImagesFolder");
        if (folder == null || folder.isEmpty()) {
            folder = BingImage.DEFAULT_BG_DIR;
        }

        if (!folder.isEmpty()) {
            File file = new File(folder);
            if (!file.exists()) {
                NotificationCenter.notice("Image folder not set");
            } else {
                String image = this.imagesHandler.getRandomImage(folder);
                if (image == null) {
                    NotificationCenter.notice("No image found");
                } else {
                    if (image.contains(",")) {
                        NotificationCenter.notice("Intellij wont load images with ',' character\n" + image);
                    }

                    prop.setValue("idea.background.frame", null);
                    prop.setValue("idea.background.editor", image);
                    IdeBackgroundUtil.repaintAllWindows();
                }
            }
        } else {
            NotificationCenter.notice("Image folder not set");
        }

        BingImage.download();
    }
}
