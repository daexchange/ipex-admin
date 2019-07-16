package ai.turbochain.ipex.model.screen;

import ai.turbochain.ipex.constant.WithdrawStatus;
import lombok.Data;

@Data
public class LegalWalletWithdrawScreen {
    WithdrawStatus status;
    String username;
    String coinName;

}
