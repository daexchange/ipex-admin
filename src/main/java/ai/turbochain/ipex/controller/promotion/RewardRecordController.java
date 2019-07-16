package ai.turbochain.ipex.controller.promotion;

import ai.turbochain.ipex.constant.PageModel;
import ai.turbochain.ipex.service.RewardRecordService;
import ai.turbochain.ipex.util.MessageResult;

import ai.turbochain.ipex.model.RewardRecordScreen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("promotion/reward-record")
public class RewardRecordController {

    @Autowired
    private RewardRecordService rewardRecordService ;

    @PostMapping("page-query")
    public MessageResult page(PageModel pageModel, RewardRecordScreen screen){
        return null;
    }
}
