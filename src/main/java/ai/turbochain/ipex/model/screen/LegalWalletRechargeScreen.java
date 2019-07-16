package ai.turbochain.ipex.model.screen;

import ai.turbochain.ipex.constant.LegalWalletState;
import lombok.Data;

@Data
public class LegalWalletRechargeScreen {
    LegalWalletState status;
    String username;
    String coinName;

}
