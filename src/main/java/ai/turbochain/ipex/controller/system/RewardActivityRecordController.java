package ai.turbochain.ipex.controller.system;

import com.querydsl.core.types.dsl.BooleanExpression;
import ai.turbochain.ipex.annotation.AccessLog;
import ai.turbochain.ipex.constant.ActivityRewardType;
import ai.turbochain.ipex.constant.AdminModule;
import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.entity.RewardActivitySetting;
import ai.turbochain.ipex.service.RewardActivitySettingService;
import ai.turbochain.ipex.util.MessageResult;

import ai.turbochain.ipex.controller.common.BaseAdminController;
import ai.turbochain.ipex.entity.QRewardActivitySetting;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/system/reward-activity-record")
public class RewardActivityRecordController extends BaseAdminController {

    @Autowired
    private RewardActivitySettingService rewardActivitySettingService;

    @RequiresPermissions("system:reward-activity-record:merge")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "创建修改邀请奖励设置")
    public MessageResult merge(@Valid RewardActivitySetting setting) {
        rewardActivitySettingService.save(setting);
        return MessageResult.success("保存成功");
    }

    /**
     * 查询所有未被禁用的（判断type条件）
     * 默认按照id降序
     *
     * @param type
     * @return
     */
    @RequiresPermissions("system:reward-activity-record:page-query")
    @GetMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "分页查询邀请奖励设置")
    public MessageResult pageQuery(PageModel pageModel,
                                   @RequestParam(value = "type", required = false) ActivityRewardType type) {
        BooleanExpression predicate = null;
        if (type != null) {
            predicate = QRewardActivitySetting.rewardActivitySetting.type.eq(type);
        }
        Page<RewardActivitySetting> all = rewardActivitySettingService.findAll(predicate, pageModel);
        return success(all);
    }

    @RequiresPermissions("system:reward-activity-record:deletes")
    @DeleteMapping("deletes")
    @AccessLog(module = AdminModule.SYSTEM, operation = "批量删除邀请奖励设置")
    public MessageResult deletes(Long[] ids) {
        Assert.notNull(ids, "ids不能为null");
        rewardActivitySettingService.deletes(ids);
        return MessageResult.success("删除成功");
    }
}
