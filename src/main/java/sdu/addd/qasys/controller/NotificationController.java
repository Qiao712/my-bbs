package sdu.addd.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.entity.Notification;
import sdu.addd.qasys.service.NotificationService;

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
