package uk.yermak.audiobookconverter.loaders;

import javafx.scene.image.Image;
import uk.yermak.audiobookconverter.book.ArtWork;
import uk.yermak.audiobookconverter.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Deprecated
public class ArtWorkBean implements ArtWork {
    private final String fileName;
    private final long crc32;

    public String fileName() {
        return this.fileName;
    }

    public long crc32() {
        return this.crc32;
    }

    public long getCrc32() {
        return this.crc32();
    }

    public String getFileName() {
        return this.fileName();
    }

    @Override
    public boolean matchCrc32(long crc32) {
        return this.crc32 == crc32;
    }

    @Override
    public Image image() {
        try {
            return  new Image(new FileInputStream(getFileName()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ArtWorkBean(final String fileName, final long crc32) {
        this.fileName = fileName;
        this.crc32 = crc32;
    }

    public ArtWorkBean(String fileName) {
        this(fileName, Utils.checksumCRC32(new File(fileName)));
    }

}

        