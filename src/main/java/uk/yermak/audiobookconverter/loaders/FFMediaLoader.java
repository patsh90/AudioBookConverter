package uk.yermak.audiobookconverter.loaders;

import javafx.scene.image.Image;
import net.bramp.ffmpeg.FFprobe;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.book.*;
import uk.yermak.audiobookconverter.fx.ConversionContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.*;

/**
 * Background media info loader. Media files are processed in the same order as passed to the constructor.
 *
 * @author yermak
 */
public class FFMediaLoader {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<String> fileNames;
    private final ConversionGroup conversionGroup;
    private static final ExecutorService mediaExecutor = Executors.newSingleThreadExecutor();

    public FFMediaLoader(List<String> files, ConversionGroup conversionGroup) {
        this.fileNames = files;
        this.conversionGroup = conversionGroup;
    }

    public List<MediaInfo> loadMediaInfo() {
        logger.info("Loading media info");
        try {
            FFprobe ffprobe = new FFprobe(Platform.FFPROBE);
            List<MediaInfo> media = new ArrayList<>();
            for (String fileName : fileNames) {
                Future<MediaInfo> futureLoad = mediaExecutor.submit(new MediaInfoLoader(ffprobe, fileName, conversionGroup));
                MediaInfo mediaInfo = new MediaInfoProxy(fileName, futureLoad);
                media.add(mediaInfo);
            }

            searchForPosters(media);

            return media;
        } catch (Exception e) {
            logger.error("Error during loading media info", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void detach() {

    }


    static Collection<File> findPictures(File dir) {
        try {
            return FileUtils.listFiles(dir, ArtWork.IMAGE_EXTENSIONS, true);
        } catch (Exception e) {
            logger.error("Failed to search for images in dir:" + dir, e);
            return Collections.emptyList();
        }
    }

    static void searchForPosters(List<MediaInfo> media) throws FileNotFoundException {
        Set<File> searchDirs = new HashSet<>();
        media.forEach(mi -> searchDirs.add(new File(mi.getFileName()).getParentFile()));

        List<File> pictures = new ArrayList<>();

        ConversionContext context = AudiobookConverter.getContext();
        for (File d : searchDirs) {
            pictures.addAll(findPictures(d));
        }

        //adding artificial limit of image count to address issue #153.
        if (!pictures.isEmpty()) {
            for (int i = 0; i < 10 && i < pictures.size(); i++) {
//                context.addPosterIfMissingWithDelay(new ArtWorkBean(Utils.tempCopy(pictures.get(i).getPath())));
                context.addPosterIfMissingWithDelay(new ArtWorkImage(new Image(new FileInputStream(pictures.get(i).getPath()))));
            }
        }
    }


}
