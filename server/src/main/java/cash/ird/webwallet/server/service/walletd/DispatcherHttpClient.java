package cash.ird.webwallet.server.service.walletd;

import cash.ird.walletd.rpc.HttpClient;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class DispatcherHttpClient extends HttpClient {

    private final String host;

    public DispatcherHttpClient(String host) {
        this.host = host;
    }


    @Override
    public <T> HttpResponse<T> post(String url, Object body, Class<? extends T> responseClass) throws UnirestException {
        return Unirest.post(url)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Host", host)
                .body(body)
                .asObject(responseClass);
    }
}
