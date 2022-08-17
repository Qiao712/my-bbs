package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.entity.FileIdentity;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface FileService extends IService<FileIdentity> {
    /**
     * 上传文件，返回文件id
     * @param path 储存路径
     * @param fileType 文件类型
     * @param inputStream 文件内容流
     * @return 返回文件标识对象
     */
    FileIdentity uploadFile(String path, String fileType, InputStream inputStream, boolean isTemporary);

    /**
     * 上传文件，返回文件id
     * @param path 储存路径
     * @param file 请求上传的文件
     * @return 返回文件标识对象
     */
    FileIdentity uploadFile(String path, MultipartFile file, boolean isTemporary);

    /**
     * 设置文件是否为临时文件
     */
    boolean setTempFlags(List<Long> fileIds, boolean isTemporary);

    /**
     * 根据文件id读取文件内容
     */
    boolean getFile(Long fileId, OutputStream outputStream);

    /**
     * 根据文件id获取用于访问该文件的url
     */
    String getFileUrl(Long fileId);

    /**
     * 根据文件id批量获取用于访问文件的url
     */
    List<String> getBatchFileUrls(List<Long> fileIds);

    /**
     * 根据文件的url或文件信息
     */
    FileIdentity getFileIdentityByUrl(String url);

    /**
     * 根据id删除文件
     */
    boolean deleteFile(Long fileId);

    /**
     * 清理临时文件
     */
    void clearTemporaryFile();
}
