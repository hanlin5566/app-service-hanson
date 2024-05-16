package com.iss.hanson.hanson.serevice.wxcp.extend;

import com.google.gson.JsonObject;
import com.iss.hanson.hanson.serevice.wxcp.WxCpConfiguration;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.util.json.GsonParser;
import me.chanjar.weixin.cp.api.WxCpService;

/**
 * @author iSoftStone-Robert
 * @date 2023/2/9
 */
public class WxCpExtServiceImpl implements WxCpExtService {

    private static final String GET_USERID_BY_EMAIL_URL = "/cgi-bin/user/get_userid_by_email";

    @Override
    public String getUseridByEmail(String email, Integer emailType) throws WxErrorException {
        final WxCpService wxCpService = WxCpConfiguration.getCpService(1000010);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("email_type", emailType != null && emailType == 2 ? 2 : 1);

        String responseContent = wxCpService.post(wxCpService.getWxCpConfigStorage().getApiUrl(GET_USERID_BY_EMAIL_URL),
                jsonObject);
        JsonObject tmpJson = GsonParser.parse(responseContent);
        return tmpJson.get("userid").getAsString();
    }

}
