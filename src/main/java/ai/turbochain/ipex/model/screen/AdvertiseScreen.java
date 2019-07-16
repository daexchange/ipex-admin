package ai.turbochain.ipex.model.screen;

import ai.turbochain.ipex.constant.AdvertiseControlStatus;
import ai.turbochain.ipex.constant.AdvertiseType;
import lombok.Data;

@Data
public class AdvertiseScreen extends AccountScreen{

    AdvertiseType advertiseType;

    String payModel ;

    /**
     * 广告状态 (012  上架/下架/关闭)
     */
    AdvertiseControlStatus status ;

}
