
package com.coleman.http.json.bussiness;

import java.util.Observer;

import com.coleman.http.json.bean.VersionCheckReq;
import com.coleman.http.json.bean.VersionCheckResp;
import com.coleman.http.json.connection.HttpHandler;
import com.coleman.http.json.connection.SLRequest;
import com.coleman.http.json.connection.SLResponse;

public class WorkManager {
    private static WorkManager manager;

    private WorkManager() {
    }

    public static WorkManager getInstance() {
        if (manager == null) {
            manager = new WorkManager();
        }
        return manager;
    }

    public SLResponse<VersionCheckResp> versionUpgrade(Observer observer,
            SLRequest<VersionCheckReq> req) {
        // first step
        HttpHandler handler = new HttpHandler();
        SLResponse<VersionCheckResp> resp = new SLResponse<VersionCheckResp>(
                VersionCheckResp.class);
        handler.setResponse(resp);
        resp.addObserver(observer);

        // last step
        handler.sendRequest(req);
        return resp;
    }

}
