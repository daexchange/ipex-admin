package ai.turbochain.ipex.controller.activity;

import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.constant.SignStatus;
import ai.turbochain.ipex.entity.Sign;
import ai.turbochain.ipex.service.CoinService;
import ai.turbochain.ipex.service.SignService;
import ai.turbochain.ipex.util.BindingResultUtil;
import ai.turbochain.ipex.util.MessageResult;

import ai.turbochain.ipex.controller.common.BaseAdminController;
import ai.turbochain.ipex.model.create.SignCreate;
import ai.turbochain.ipex.model.screen.SignScreen;
import ai.turbochain.ipex.model.update.SignUpdate;
import ai.turbochain.ipex.model.vo.SignVO;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author GS
 * @Description:
 * @date 2018/5/311:22
 */
@RestController
@RequestMapping("activity/sign")
public class SignController extends BaseAdminController {
    @Autowired
    private SignService service;
    @Autowired
    private CoinService coinService;

    //创建
    @RequiresPermissions("activity:sign:post")
    @PostMapping
    public MessageResult create(@Valid SignCreate model, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        service.save(model.transformation(service, coinService));
        return success();
    }

    //更新
    @RequiresPermissions("activity:sign:put")
    @PutMapping("{id}")
    public MessageResult update(@Valid SignUpdate model, BindingResult bindingResult, @PathVariable("id") Long id) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        //校验id
        Sign sign = service.findById(id);
        Assert.notNull(sign, "validate id!");
        //转化
        sign = model.transformation(coinService, sign);
        //保存
        service.save(sign);
        return success();
    }

    //分页
    @RequiresPermissions("activity:sign:page-query")
    @GetMapping("page-query")
    public MessageResult pageQuery(SignScreen screen, PageModel pageModel) {
        Page<Sign> source = service.findAllScreen(screen, pageModel);
        Page<SignVO> page = source.map(x -> SignVO.getSignVO(x));
        return success(page);
    }

    //详情
    @RequiresPermissions("activity:sign:id:get")
    @GetMapping("{id}")
    public MessageResult detail(@PathVariable("id") Long id) {
        //校验id
        Sign sign = service.findById(id);
        Assert.notNull(sign, "validate id!");
        SignVO signVO = SignVO.getSignVO(sign);
        return success(signVO);
    }

    // 提前关闭
    @RequiresPermissions("activity:sign:id:early-closing")
    @PatchMapping("{id}/early-closing")
    public MessageResult earlyClosing(@PathVariable("id") Long id) {
        //校验id
        Sign sign = service.findById(id);
        Assert.notNull(sign, "validate id!");
        Assert.isTrue(sign.getStatus() == SignStatus.UNDERWAY, "validate status!");
        service.earlyClosing(sign);
        return success();
    }

    //判断是否存在惊醒中
    @GetMapping("has-underway")
    public MessageResult is() {
        Sign sign = service.fetchUnderway();
        if (sign != null) {
            return success(true);
        } else {
            return success(false);
        }
    }

}