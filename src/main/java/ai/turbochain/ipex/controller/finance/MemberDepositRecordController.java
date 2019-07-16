package ai.turbochain.ipex.controller.finance;


import com.querydsl.core.types.dsl.BooleanExpression;
import ai.turbochain.ipex.annotation.AccessLog;
import ai.turbochain.ipex.constant.AdminModule;
import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberDepositService;
import ai.turbochain.ipex.util.MessageResult;
import ai.turbochain.ipex.vo.MemberDepositVO;

import ai.turbochain.ipex.controller.common.BaseAdminController;
import ai.turbochain.ipex.entity.QMember;
import ai.turbochain.ipex.entity.QMemberDeposit;
import ai.turbochain.ipex.model.screen.MemberDepositScreen;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("finance/member-deposit")
public class MemberDepositRecordController extends BaseAdminController {
    @Autowired
    private MemberDepositService memberDepositService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * 充币记录
     *
     * @param pageModel
     * @param screen
     * @return
     */
    @RequiresPermissions("finance:member-deposit:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "充币记录")
    public MessageResult page(PageModel pageModel, MemberDepositScreen screen) {
        List<BooleanExpression> predicates = new ArrayList<>();
        predicates.add(QMember.member.id.eq(QMemberDeposit.memberDeposit.memberId));
        if (!StringUtils.isEmpty(screen.getUnit())) {
            predicates.add((QMemberDeposit.memberDeposit.unit.equalsIgnoreCase(screen.getUnit())));
        }
        if (!StringUtils.isEmpty(screen.getAddress())) {
            predicates.add((QMemberDeposit.memberDeposit.address.eq(screen.getAddress())));
        }
        if (!StringUtils.isEmpty(screen.getAccount())) {
            predicates.add(QMember.member.username.like("%" + screen.getAccount() + "%")
                    .or(QMember.member.realName.like("%" + screen.getAccount() + "%")));
        }
        Page<MemberDepositVO> page = memberDepositService.page(predicates, pageModel);
        return success(messageSource.getMessage("SUCCESS"), page);
    }
}
