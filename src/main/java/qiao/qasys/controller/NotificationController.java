package qiao.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qiao.qasys.common.PageQuery;
import qiao.qasys.common.Result;
import qiao.qasys.entity.Notification;
import qiao.qasys.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<Notification>> listNotifications(@Validated PageQuery pageQuery){
        return Result.succeed(notificationService.listNotifications(pageQuery, null));
    }
}
