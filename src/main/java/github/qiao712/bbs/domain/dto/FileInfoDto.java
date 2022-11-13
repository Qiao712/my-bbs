package github.qiao712.bbs.domain.dto;

import github.qiao712.bbs.domain.entity.FileIdentity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileInfoDto extends FileIdentity {
    private String uploaderUsername;
    private String url;
}
