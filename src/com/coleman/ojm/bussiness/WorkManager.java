
package com.coleman.ojm.bussiness;

import java.util.Collection;

import com.coleman.ojm.bean.LoginReq;
import com.coleman.ojm.bean.LoginResp;
import com.coleman.ojm.bean.VersionCheckReq;
import com.coleman.ojm.bean.VersionCheckResp;
import com.coleman.ojm.bean.WordlistReq;
import com.coleman.ojm.bean.WordlistResp;
import com.coleman.ojm.core.Observer;
import com.coleman.ojm.http.HttpHandler;
import com.coleman.ojm.http.SLRequest;
import com.coleman.ojm.http.SLResponse;

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

    public SLResponse<VersionCheckResp> versionUpgrade(SLRequest<VersionCheckReq> req) {
        // first step
        HttpHandler handler = new HttpHandler();
        SLResponse<VersionCheckResp> resp = new SLResponse<VersionCheckResp>(VersionCheckResp.class);
        handler.setResponse(resp);
        Collection<Observer> obs = req.getObservers();
        resp.addObservers(obs);

        // last step
        handler.sendRequest(req);
        return resp;
    }

    public SLResponse<WordlistResp> getWordlist(SLRequest<WordlistReq> slReq) {
        // first step
        HttpHandler handler = new HttpHandler();
        SLResponse<WordlistResp> resp = new SLResponse<WordlistResp>(WordlistResp.class);
        handler.setResponse(resp);
        Collection<Observer> obs = slReq.getObservers();
        resp.addObservers(obs);

        // last step
        handler.sendRequest(slReq);
        return resp;
    }

    public SLResponse<LoginResp> login(SLRequest<LoginReq> slReq) {
        // first step
        HttpHandler handler = new HttpHandler();
        SLResponse<LoginResp> resp = new SLResponse<LoginResp>(LoginResp.class);
        handler.setResponse(resp);
        Collection<Observer> obs = slReq.getObservers();
        resp.addObservers(obs);

        // last step
        handler.sendRequest(slReq);
        return resp;
    }

}
