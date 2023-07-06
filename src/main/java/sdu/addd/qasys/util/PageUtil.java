package sdu.addd.qasys.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public class PageUtil {
    /**
     * 更换IPage的内容
     */
    @SuppressWarnings("unchecked")
    public static <T, S> IPage<T> replaceRecords(IPage<S> page, List<T> records){
        Page<T> tPage = (Page<T>) page;
        tPage.setRecords(records);
        return tPage;
    }
}