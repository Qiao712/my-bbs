package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.entity.FileIdentity;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileService extends IService<FileIdentity> {
    /**
     * 上传文件，返回文件id
     */
    Long saveFile(String dir, String fileType, InputStream inputStream);

    Long saveFile(String dir, MultipartFile file);

    String getFileUrl(Long id);

    boolean deleteFile(Long id);

    boolean getFile(Long id, OutputStream outputStream);
}
