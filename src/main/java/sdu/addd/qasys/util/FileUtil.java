package sdu.addd.qasys.util;

import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.MimeType;

import java.util.Optional;

public class FileUtil {
    /**
     * 根据文件名(拓展名)判断是否为图片文件
     */
    public static boolean isPictureFile(String filename){
        if(filename == null) return false;
        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType("." + filename);
        return mediaType.isPresent() && mediaType.get().getType().equals("image");
    }

    /**
     * 获取文件后缀(拓展名)
     */
    public static String getSuffix(String filename){
        if(filename == null) return null;

        String[] parts = filename.split("\\.");
        if(parts.length > 1){
            return parts[parts.length - 1];
        }else{
            return null;
        }
    }

    /**
     * 根据文件名(拓展名)判断ContentType
     */
    public static String getContentType(String pictureFormat){
        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType("." + pictureFormat);
        return mediaType.map(MimeType::toString).orElse(null);
    }
}
