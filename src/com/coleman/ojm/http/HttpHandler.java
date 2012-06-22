
package com.coleman.ojm.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

import com.coleman.kingword.R;
import com.coleman.ojm.annotation.RequestObject;
import com.coleman.ojm.annotation.ResponseObject;
import com.coleman.ojm.core.SLParser;
import com.coleman.ojm.core.SLWrapper;
import com.coleman.ojm.exception.FieldNotInitException;
import com.coleman.ojm.exception.UnknownTypeException;
import com.coleman.util.Config;
import com.coleman.util.MyApp;
import com.coleman.util.ThreadUtils;

public class HttpHandler {
    private static final String TAG = "HttpHandler";

    private static final LinkedList<SLRequest<?>> cancelableList = new LinkedList<SLRequest<?>>();

    private static final LinkedList<SLRequest<?>> backgroundList = new LinkedList<SLRequest<?>>();

    private SLResponse<?> mSLResponse;

    public HttpHandler() {
    }

    /**
     * If quit from the application, need to cancel all the SLRequest.
     */
    public static void cancelAllRequests() {
        for (int i = 0; i < cancelableList.size(); i++) {
            SLRequest<?> req = cancelableList.get(i);
            req.setCancel(true);
            req.disconnect();
        }
        for (int i = 0; i < backgroundList.size(); i++) {
            SLRequest<?> req = backgroundList.get(i);
            req.setCancel(true);
            req.disconnect();
        }
    }

    /**
     * Cancel all the foreground request.
     */
    public static void cancelForegroundRequests() {
        for (int i = 0; i < cancelableList.size(); i++) {
            SLRequest<?> req = cancelableList.get(i);
            req.setCancel(true);
            req.disconnect();
        }
    }

    /**
     * This request can not be cancel by user.
     * 
     * @param slRequest
     */
    public void sendBackgroundRequest(SLRequest<?> slRequest) {
        sendRequest(slRequest, true);
    }

    /**
     * This request can be cancel if needed.
     * 
     * @param slRequest
     */
    public void sendRequest(SLRequest<?> slRequest)

    {
        sendRequest(slRequest, false);
    }

    public SLResponse<?> getResponse() {
        return mSLResponse;
    }

    public void setResponse(SLResponse<?> slResp) {
        this.mSLResponse = slResp;
        if (mSLResponse.getResponse().getClass().getAnnotation(ResponseObject.class) == null) {
            throw new RuntimeException("Wrong response object type: "
                    + mSLResponse.getResponse().getClass().getName());
        }
    }

    /**
     * send the request to server.
     * 
     * @param slRequest SLRequest object.
     * @param background mark if add the SLRequest to the processing list.
     */
    private void sendRequest(final SLRequest<?> slRequest, final boolean background) {

        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String notifyMsg = "";
                HttpURLConnection con = null;
                try {
                    if (slRequest == null) {
                        throw new RuntimeException("The SLRequest object is null");
                    }

                    // deal if the background request
                    slRequest.setCancel(false);
                    if (background) {
                        backgroundList.add(slRequest);
                    } else {
                        cancelableList.add(slRequest);

                    }

                    // sleep 50 ms to let the ui thread run a while
                    Thread.sleep(3550);

                    if (!slRequest.getRequest().getClass().isAnnotationPresent(RequestObject.class)) {
                        throw new RuntimeException("Wrong request object type: "
                                + slRequest.getRequest().getClass().getName());
                    }

                    String url = "";
                    String path = "";
                    if (Config.isTestServer) {
                        url = slRequest.getRequest().getClass().getAnnotation(RequestObject.class)
                                .url();
                    } else {
                        url = slRequest.getRequest().getClass().getAnnotation(RequestObject.class)
                                .hwUrl();
                    }
                    path = slRequest.getRequest().getClass().getAnnotation(RequestObject.class)
                            .path();
                    url = (url.length() > 0 && url.endsWith("/")) ? url.substring(0,
                            url.length() - 1) : url;
                    path = path.startsWith("/") ? path : "/" + path;

                    Log.i(TAG, "===connect: " + (url + path));

                    con = (HttpURLConnection) new URL(url + path).openConnection();
                    // save the connection to the slRequest
                    slRequest.setConnection(con);
                    slRequest.startTimeOutCount();
                    con.addRequestProperty("Accept-Encoding", "gzip");
                    con.addRequestProperty("charset", "UTF-8");
                    con.setRequestMethod("POST");
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.connect();
                    postRequest(con.getOutputStream(), slRequest);
                    int rc = con.getResponseCode();
                    // remove the connection from the slRequest
                    slRequest.setConnection(null);
                    if (slRequest.isCancel()) {
                        Log.i(TAG, "The request has been canceled!");
                        return;
                    }
                    if (rc == 200) {
                        parseHttpResponse(con.getInputStream());
                    } else {
                        throw new IOException("Response code is " + rc + " not 200!");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_unknow);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_url);
                } catch (IOException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_network);

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_unknow);
                } catch (JSONException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_resp_data);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_unknow);
                } catch (UnknownTypeException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_req_data);
                } catch (FieldNotInitException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_init);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_unknow);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_unknow);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_unknow);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    notifyMsg = MyApp.context.getString(R.string.error_unknow);
                } finally {
                    // remove the slRequest from the buffered list.
                    if (background) {
                        backgroundList.remove(slRequest);
                    } else {
                        cancelableList.remove(slRequest);
                    }

                    try {
                        con.disconnect();
                    } catch (Exception e2) {
                    }
                }

                if (!slRequest.isCancel() && !TextUtils.isEmpty(notifyMsg)) {
                    mSLResponse.notifyError(notifyMsg);
                }
            }

        });
    }

    /**
     * Wrap SLRequest to a HttpPost request.
     * 
     * @param outputStream
     * @param slRequest
     * @return
     * @throws IllegalArgumentException
     * @throws JSONException
     * @throws IllegalAccessException
     * @throws UnknownTypeException
     * @throws FieldNotInitException
     * @throws IOException
     */
    private void postRequest(OutputStream outputStream, SLRequest<?> slRequest)
            throws IllegalArgumentException, JSONException, IllegalAccessException,
            UnknownTypeException, FieldNotInitException, IOException {
        JSONObject json = SLWrapper.wrap(slRequest);
        Log.i(TAG, "---request: " + slRequest.getRequest().getClass().getName() + "\n===reqStr:"
                + json.toString());
        byte bytes[] = json.toString().getBytes("UTF-8");

        GZIPOutputStream gos = new GZIPOutputStream(outputStream);
        gos.write(bytes);

        gos.flush();
        gos.close();
    }

    /**
     * Parse response Object to the T object.
     * 
     * @param response
     * @throws IOException
     * @throws IllegalStateException
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws UnknownTypeException
     * @throws FieldNotInitException
     * @throws ClassNotFoundException
     */
    private void parseHttpResponse(InputStream is) throws IllegalStateException, IOException,
            JSONException, IllegalArgumentException, IllegalAccessException,
            InstantiationException, UnknownTypeException, FieldNotInitException,
            ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buf[] = new byte[8 * 1024];
        InputStream tempIn = is;
        tempIn = new GZIPInputStream(is);

        for (int i = tempIn.read(buf); i > 0; i = tempIn.read(buf)) {
            baos.write(buf, 0, i);
        }
        baos.flush();
        String respStr = baos.toString();

        try {
            baos.close();
            tempIn.close();
        } catch (Exception e) {
        }
        Log.i(TAG, "---response: " + mSLResponse.getResponse().getClass().getName()
                + "\n===respStr: " + respStr);
        JSONObject jsonRespObj = new JSONObject(new JSONTokener(respStr));
        SLParser.parse(mSLResponse, jsonRespObj);
    }
}
