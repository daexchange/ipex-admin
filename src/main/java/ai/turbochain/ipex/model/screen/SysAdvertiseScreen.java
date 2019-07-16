package ai.turbochain.ipex.model.screen;

import ai.turbochain.ipex.constant.CommonStatus;
import ai.turbochain.ipex.constant.SysAdvertiseLocation;
import lombok.Data;

@Data
public class SysAdvertiseScreen {
    private String serialNumber;
    private SysAdvertiseLocation sysAdvertiseLocation;
    private CommonStatus status;
}
