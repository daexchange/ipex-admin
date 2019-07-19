package ai.turbochain.ipex.controller.finance;

import static ai.turbochain.ipex.constant.BooleanEnum.IS_FALSE;
import static ai.turbochain.ipex.constant.WithdrawStatus.FAIL;
import static ai.turbochain.ipex.constant.WithdrawStatus.SUCCESS;
import static ai.turbochain.ipex.constant.WithdrawStatus.WAITING;
import static ai.turbochain.ipex.util.BigDecimalUtils.sub;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.sparkframework.security.Encrypt;

import ai.turbochain.ipex.annotation.AccessLog;
import ai.turbochain.ipex.constant.AdminModule;
import ai.turbochain.ipex.constant.BooleanEnum;
import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.constant.SysConstant;
import ai.turbochain.ipex.constant.TransactionType;
import ai.turbochain.ipex.constant.WithdrawStatus;
import ai.turbochain.ipex.controller.common.BaseAdminController;
import ai.turbochain.ipex.entity.Admin;
import ai.turbochain.ipex.entity.Coin;
import ai.turbochain.ipex.entity.Member;
import ai.turbochain.ipex.entity.MemberTransaction;
import ai.turbochain.ipex.entity.MemberWallet;
import ai.turbochain.ipex.entity.QMember;
import ai.turbochain.ipex.entity.QWithdrawRecord;
import ai.turbochain.ipex.entity.WithdrawRecord;
import ai.turbochain.ipex.es.ESUtils;
import ai.turbochain.ipex.model.screen.WithdrawRecordScreen;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.service.MemberService;
import ai.turbochain.ipex.service.MemberTransactionService;
import ai.turbochain.ipex.service.MemberWalletService;
import ai.turbochain.ipex.service.WithdrawRecordService;
import ai.turbochain.ipex.util.MessageResult;
import ai.turbochain.ipex.vo.WithdrawRecordVO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author GS
 * @description 提现
 * @date 2018/2/25 11:22
 */
@RestController
@Slf4j
@RequestMapping("/finance/withdraw-record")
public class WithdrawRecordController extends BaseAdminController {
    @Autowired
    private WithdrawRecordService withdrawRecordService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private ESUtils esUtils;

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @Value("${spark.system.md5.key}")
    private String md5Key;

    @RequiresPermissions("finance:withdraw-record:all")
    @GetMapping("/all")
    @AccessLog(module = AdminModule.FINANCE, operation = "所有提现记录WithdrawRecord")
    public MessageResult all() {
        List<WithdrawRecord> withdrawRecordList = withdrawRecordService.findAll();
        if (withdrawRecordList == null || withdrawRecordList.size() < 1) {
            return error(messageSource.getMessage("NO_DATA"));
        }
        return success(withdrawRecordList);
    }

    @RequiresPermissions(value = {"finance:withdraw-record:page-query", "finance:withdraw-record:page-query:success"}, logical = Logical.OR)
    @RequestMapping("/page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "分页查询提现记录WithdrawRecord")
    public MessageResult pageQuery(
            PageModel pageModel,
            WithdrawRecordScreen screen) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(QWithdrawRecord.withdrawRecord.memberId.eq(QMember.member.id));

        if (screen.getMemberId() != null) {
            predicates.add(QWithdrawRecord.withdrawRecord.memberId.eq(screen.getMemberId()));
        }

        if ( !StringUtils.isEmpty(screen.getMobilePhone())){
            Member member = memberService.findByPhone(screen.getMobilePhone());
            predicates.add(QWithdrawRecord.withdrawRecord.memberId.eq(member.getId()));
        }

        if ( !StringUtils.isEmpty(screen.getOrderSn())){
            predicates.add(QWithdrawRecord.withdrawRecord.transactionNumber.eq(screen.getOrderSn()));
        }

        if (screen.getStatus() != null) {
            predicates.add(QWithdrawRecord.withdrawRecord.status.eq(screen.getStatus()));
        }

        if (screen.getIsAuto() != null) {
            predicates.add(QWithdrawRecord.withdrawRecord.isAuto.eq(screen.getIsAuto()));
        }

        if (!StringUtils.isEmpty(screen.getAddress())) {
            predicates.add(QWithdrawRecord.withdrawRecord.address.eq(screen.getAddress()));
        }

        if (!StringUtils.isEmpty(screen.getUnit())) {
            predicates.add(QWithdrawRecord.withdrawRecord.coin.unit.equalsIgnoreCase(screen.getUnit()));
        }

        if (!StringUtils.isEmpty(screen.getAccount())) {
            predicates.add(QMember.member.username.like("%" + screen.getAccount() + "%")
                    .or(QMember.member.realName.like("%" + screen.getAccount() + "%")));
        }

        Page<WithdrawRecordVO> pageListMapResult = withdrawRecordService.joinFind(predicates, pageModel);
        return success(pageListMapResult);
    }

    @GetMapping("/{id}")
    @RequiresPermissions("finance:withdraw-record:detail")
    @AccessLog(module = AdminModule.FINANCE, operation = "提现记录WithdrawRecord 详情")
    public MessageResult detail(@PathVariable("id") Long id) {
        WithdrawRecord withdrawRecord = withdrawRecordService.findOne(id);
        notNull(withdrawRecord, messageSource.getMessage("NO_DATA"));
        return success(withdrawRecord);
    }

    //一键审核通过
    @RequiresPermissions("finance:withdraw-record:audit-pass")
    @PatchMapping("/audit-pass")
    @AccessLog(module = AdminModule.FINANCE, operation = "提现记录WithdrawRecord一键审核通过")
    public MessageResult auditPass(@RequestParam("ids") Long[] ids) {
        withdrawRecordService.audit(ids, WAITING);
        return success(messageSource.getMessage("PASS_THE_AUDIT"));
    }

    //一键审核不通过
    @RequiresPermissions("finance:withdraw-record:audit-no-pass")
    @PatchMapping("/audit-no-pass")
    @AccessLog(module = AdminModule.FINANCE, operation = "提现记录WithdrawRecord一键审核不通过")
    public MessageResult auditNoPass(@RequestParam("ids") Long[] ids) {
        withdrawRecordService.audit(ids, FAIL);
        return success(messageSource.getMessage("AUDIT_DOES_NOT_PASS"));
    }
    
    /**
             * 单个打款 转账成功添加流水号
     *
     * @param id
     * @param transactionNumber
     * @return
     */
    @RequiresPermissions("finance:withdraw-record:add-transaction-number")
    @PatchMapping("/add-transaction-number")
    @AccessLog(module = AdminModule.FINANCE, operation = "添加交易流水号")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult addNumber(
            @RequestParam("id") Long id,
            @RequestParam("transactionNumber") String transactionNumber) {
        WithdrawRecord record = withdrawRecordService.findOne(id);
        Assert.notNull(record, "该记录不存在");
        Assert.isTrue(record.getIsAuto() == BooleanEnum.IS_FALSE, "该提现单为自动审核");
        record.setTransactionNumber(transactionNumber);
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(record.getCoin(), record.getMemberId());
        Assert.notNull(memberWallet, "member id " + record.getMemberId() + " 的 wallet 为 null");
       
        record.setStatus(WithdrawStatus.WAITING);
        WithdrawRecord withdrawRecord = withdrawRecordService.save(record);
        JSONObject json = new JSONObject();
        json.put("uid", record.getMemberId());
        //提币总数量
        json.put("totalAmount", record.getTotalAmount());
        //手续费
        json.put("fee", record.getFee());
        //预计到账数量
        json.put("arriveAmount", record.getArrivedAmount());
        //币种
        json.put("coin", record.getCoin());
        //提币地址
        json.put("address", record.getAddress());
        //提币记录id
        json.put("withdrawId", withdrawRecord.getId());
        kafkaTemplate.send("withdraw", record.getCoin().getUnit(), json.toJSONString());
        return MessageResult.success(messageSource.getMessage("APPLY_SUCCESS"));
    }

    /**
     * 单个打款 转账成功添加流水号  备份
     *
     * @param id
     * @param transactionNumber
     * @return
     */
    public MessageResult addNumberBack(
            @RequestParam("id") Long id,
            @RequestParam("transactionNumber") String transactionNumber) {
        WithdrawRecord record = withdrawRecordService.findOne(id);
        Assert.notNull(record, "该记录不存在");
        Assert.isTrue(record.getIsAuto() == BooleanEnum.IS_FALSE, "该提现单为自动审核");
        record.setTransactionNumber(transactionNumber);
        record.setStatus(WithdrawStatus.SUCCESS);
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(record.getCoin(), record.getMemberId());
        Assert.notNull(memberWallet, "member id " + record.getMemberId() + " 的 wallet 为 null");
        memberWallet.setFrozenBalance(memberWallet.getFrozenBalance().subtract(record.getTotalAmount()));
        memberWalletService.save(memberWallet);
        record = withdrawRecordService.save(record);
        
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setMemberId(record.getMemberId());
        memberTransaction.setAddress(record.getAddress());
        memberTransaction.setAmount(record.getTotalAmount());
        memberTransaction.setSymbol(record.getCoin().getUnit());
        memberTransaction.setCreateTime(record.getCreateTime());
        memberTransaction.setType(TransactionType.WITHDRAW);
        memberTransaction.setFee(record.getFee());
        memberTransaction.setRealFee(record.getFee()+"");
        memberTransaction.setDiscountFee("0");
        memberTransaction= memberTransactionService.save(memberTransaction);

        return MessageResult.success(messageSource.getMessage("SUCCESS"), record);
    }

    //批量打款
    @RequiresPermissions("finance:withdraw-record:remittance")
    @PatchMapping("/remittance")
    @AccessLog(module = AdminModule.FINANCE, operation = "提现记录/批量打款")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult remittance(
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
            @RequestParam("ids") Long[] ids,
            @RequestParam("transactionNumber") String transactionNumber,
            @RequestParam("password") String password) {
        Assert.notNull(admin, messageSource.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));
        password = Encrypt.MD5(password + md5Key);
        if (!password.equals(admin.getPassword())) {
            return error(messageSource.getMessage("WRONG_PASSWORD"));
        }
        WithdrawRecord withdrawRecord;
        for (Long id : ids) {
            withdrawRecord = withdrawRecordService.findOne(id);
            notNull(withdrawRecord, "id :" + id + messageSource.getMessage("NO_DATA"));
            isTrue(withdrawRecord.getStatus() == WAITING, "提现状态不是等待放币,不能打款!");
            isTrue(withdrawRecord.getIsAuto() == IS_FALSE, "不是人工审核提现!");
            //标记提现完成
            withdrawRecord.setStatus(SUCCESS);
            //交易编码
            withdrawRecord.setTransactionNumber(transactionNumber);
            MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(withdrawRecord.getCoin(), withdrawRecord.getMemberId());
            Assert.notNull(memberWallet, "member id " + withdrawRecord.getMemberId() + " 的 wallet 为 null");
            memberWallet.setFrozenBalance(memberWallet.getFrozenBalance().subtract(withdrawRecord.getTotalAmount()));
            memberWalletService.save(memberWallet);
            withdrawRecordService.save(withdrawRecord);

            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setMemberId(withdrawRecord.getMemberId());
            memberTransaction.setAddress(withdrawRecord.getAddress());
            memberTransaction.setAmount(withdrawRecord.getTotalAmount());
            memberTransaction.setSymbol(withdrawRecord.getCoin().getUnit());
            memberTransaction.setCreateTime(withdrawRecord.getCreateTime());
            memberTransaction.setType(TransactionType.WITHDRAW);
            memberTransaction.setFee(withdrawRecord.getFee());
            memberTransaction.setRealFee(withdrawRecord.getFee()+"");
            memberTransaction.setDiscountFee("0");
            memberTransaction= memberTransactionService.save(memberTransaction);

        }
        return success();
    }

}
