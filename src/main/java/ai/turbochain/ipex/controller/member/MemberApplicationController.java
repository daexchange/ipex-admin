package ai.turbochain.ipex.controller.member;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import ai.turbochain.ipex.annotation.AccessLog;
import ai.turbochain.ipex.constant.AdminModule;
import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.entity.MemberApplication;
import ai.turbochain.ipex.entity.QBusinessCancelApply;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberApplicationService;
import ai.turbochain.ipex.util.MessageResult;
import ai.turbochain.ipex.util.PredicateUtils;

import ai.turbochain.ipex.controller.common.BaseAdminController;
import ai.turbochain.ipex.model.screen.MemberApplicationScreen;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static ai.turbochain.ipex.constant.AuditStatus.AUDIT_ING;
import static ai.turbochain.ipex.constant.AuditStatus.AUDIT_DEFEATED;
import static ai.turbochain.ipex.constant.AuditStatus.AUDIT_SUCCESS;
import static ai.turbochain.ipex.entity.QMemberApplication.memberApplication;
import static org.springframework.util.Assert.notNull;

/**
 * @author GS
 * @description 实名审核单
 * @date 2017/12/26 15:05
 */
@RestController
@RequestMapping("member/member-application")
public class MemberApplicationController extends BaseAdminController {

    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("member:member-application:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.MEMBER, operation = "所有会员MemberApplication认证信息")
    public MessageResult all() {
        List<MemberApplication> all = memberApplicationService.findAll();
        if (all != null && all.size() > 0) {
            return success(all);
        }
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions("member:member-application:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证信息详情")
    public MessageResult detail(@RequestParam("id") Long id) {
        MemberApplication memberApplication = memberApplicationService.findOne(id);
        notNull(memberApplication, "validate id!");
        return success(memberApplication);
    }

    @RequiresPermissions("member:member-application:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.MEMBER, operation = "分页查找会员MemberApplication认证信息")
    public MessageResult queryPage(PageModel pageModel, MemberApplicationScreen screen) {
        List<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getAuditStatus() != null) {
            booleanExpressions.add(memberApplication.auditStatus.eq(screen.getAuditStatus()));
        } else {
        	booleanExpressions.add(memberApplication.auditStatus.in(AUDIT_ING, AUDIT_DEFEATED, AUDIT_SUCCESS));
        }
        if (!StringUtils.isEmpty(screen.getAccount())) {
            booleanExpressions.add(memberApplication.member.username.like("%" + screen.getAccount() + "%")
                    //.or(memberApplication.member.mobilePhone.like(screen.getAccount() + "%"))
                   // .or(memberApplication.member.email.like(screen.getAccount() + "%"))
                    .or(memberApplication.member.realName.like("%" + screen.getAccount() + "%")));
        }
        if(!StringUtils.isEmpty(screen.getCardNo())) {
            booleanExpressions.add(memberApplication.member.idNumber.like("%" + screen.getCardNo() + "%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<MemberApplication> all = memberApplicationService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("member:member-application:pass")
    @PatchMapping("{id}/pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证通过审核")
    public MessageResult pass(@PathVariable("id") Long id) {
        //校验
        MemberApplication application = memberApplicationService.findOne(id);
        notNull(application, "validate id!");
        //业务
        memberApplicationService.auditPass(application);
        //返回
        return success();
    }

    @RequiresPermissions("member:member-application:no-pass")
    @PatchMapping("{id}/no-pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证不通过审核")
    public MessageResult noPass(
            @PathVariable("id") Long id,
            @RequestParam(value = "rejectReason", required = false) String rejectReason) {
        //校验
        MemberApplication application = memberApplicationService.findOne(id);
        notNull(application, "validate id!");
        //业务
        application.setRejectReason(rejectReason);//拒绝原因
        memberApplicationService.auditNotPass(application);
        //返回
        return success();
    }
}
