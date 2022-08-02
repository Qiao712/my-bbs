package github.qiao712.bbs.domain.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import javax.validation.constraints.Max;

/**
 * 分页查询
 */
@Data
public class PageQuery {
    private final static int MAX_PAGE_SIZE = 50;
    private int pageNo = 1;

    @Max(MAX_PAGE_SIZE)
    private int pageSize = 10;

    /**
     * 转换为MyBatisPlus的分页模型
     * @return
     */
    public <T> IPage<T> getIPage(){
        Page<T> page = new Page<>();
        page.setSize(pageSize);
        page.setCurrent(pageNo);
        return page;
    }
}
