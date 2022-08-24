package github.qiao712.bbs.domain.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.qiao712.bbs.exception.ServiceException;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.Set;

/**
 * 分页查询
 */
@Data
public class PageQuery {
    private final static int MAX_PAGE_SIZE = 50;
    private int pageNo = 1;

    @Max(MAX_PAGE_SIZE)
    private int pageSize = 10;

    private String orderBy;

    private String order;   //ASC / DESC

    /**
     * 转换为MyBatisPlus的分页模型
     * (不带排序)
     */
    public <T> IPage<T> getIPage(){
        Page<T> page = new Page<>();
        page.setSize(pageSize);
        page.setCurrent(pageNo);
        return page;
    }

    /**
     * 转换为MyBatisPlus的分页模型，带排序
     * @param columnsCanSorted 允许排序的列表
     * @param defaultColumn 默认使用的排序列
     * @param defaultAsc  默认的顺序(true: asc; false: desc)
     */
    public <T> IPage<T> getIPage(Set<String> columnsCanSorted, String defaultColumn, boolean defaultAsc){
        if(orderBy != null && !columnsCanSorted.contains(orderBy)){
            throw new ServiceException("不允许按" + orderBy + "排序");
        }

        Page<T> page = new Page<>();
        page.setSize(pageSize);
        page.setCurrent(pageNo);

        boolean asc = defaultAsc;
        if(order != null){
            order = order.toLowerCase();
            if("asc".equals(order)){
                asc = true;
            }else if("desc".equals(order)){
                asc = false;
            }
        }

        page.addOrder(new OrderItem(orderBy != null ? orderBy : defaultColumn, asc));
        return page;
    }
}
