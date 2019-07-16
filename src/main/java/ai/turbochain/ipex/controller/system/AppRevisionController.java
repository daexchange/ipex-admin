package ai.turbochain.ipex.controller.system;

import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.entity.AppRevision;
import ai.turbochain.ipex.service.AppRevisionService;
import ai.turbochain.ipex.util.BindingResultUtil;
import ai.turbochain.ipex.util.MessageResult;

import ai.turbochain.ipex.controller.common.BaseAdminController;
import ai.turbochain.ipex.model.create.AppRevisionCreate;
import ai.turbochain.ipex.model.screen.AppRevisionScreen;
import ai.turbochain.ipex.model.update.AppRevisionUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author GS
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:31
 */
@RestController
@RequestMapping("system/app-revision")
public class AppRevisionController extends BaseAdminController {
    @Autowired
    private AppRevisionService service;

    //新增
    @PostMapping
    public MessageResult create(@Valid AppRevisionCreate model, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        service.save(model);
        return success();
    }

    //更新
    @PutMapping("{id}")
    public MessageResult put(@PathVariable("id") Long id, AppRevisionUpdate model) {
        AppRevision appRevision = service.findById(id);
        Assert.notNull(appRevision, "validate appRevision id!");
        service.update(model, appRevision);
        return success();
    }

    //详情
    @GetMapping("{id}")
    public MessageResult get(@PathVariable("id") Long id) {
        AppRevision appRevision = service.findById(id);
        Assert.notNull(appRevision, "validate appRevision id!");
        return success(appRevision);
    }

    //分页
    @GetMapping("page-query")
    public MessageResult get(PageModel pageModel, AppRevisionScreen screen) {
        return success(service.findAllScreen(screen, pageModel));
    }
}
