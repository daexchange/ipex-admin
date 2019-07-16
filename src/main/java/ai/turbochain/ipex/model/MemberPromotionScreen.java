package ai.turbochain.ipex.model;

import ai.turbochain.ipex.model.screen.AccountScreen;
import lombok.Data;

@Data
public class MemberPromotionScreen extends AccountScreen{

    private int minPromotionNum = -1;

    private int maxPromotionNum = -1;
}
