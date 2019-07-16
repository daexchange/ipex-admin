package ai.turbochain.ipex.controller.activity;

import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.entity.MemberSignRecord;
import ai.turbochain.ipex.service.MemberSignRecordService;
import ai.turbochain.ipex.util.MessageResult;

import ai.turbochain.ipex.controller.common.BaseAdminController;
import ai.turbochain.ipex.model.screen.MemberSignRecordScreen;
import ai.turbochain.ipex.model.vo.MemberSignRecordVO;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author GS
 * @Description: 会员签到记录
 * @date 2018/5/410:27
 */
@RestController
@RequestMapping("activity/member-sign-record")
public class MemberSignRecordControler extends BaseAdminController {
    @Autowired
    private MemberSignRecordService service;

    @RequiresPermissions("activity:member-sign-record:page-query")
    @GetMapping("page-query")
    public MessageResult pageQuery(MemberSignRecordScreen screen, PageModel pageModel) {
        Page<MemberSignRecord> source = service.findAllScreen(screen, pageModel);
        Page<MemberSignRecordVO> page = source.map(x -> MemberSignRecordVO.getMemberSignRecordVO(x));
        return success(page);
    }
}
