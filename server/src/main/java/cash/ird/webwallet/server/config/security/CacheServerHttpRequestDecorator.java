package cash.ird.webwallet.server.config.security;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import static reactor.core.scheduler.Schedulers.single;

class CacheServerHttpRequestDecorator extends ServerHttpRequestDecorator {


    CacheServerHttpRequestDecorator(ServerHttpRequest delegate) {
        super(delegate);
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody()
            .publishOn(single())
            .cache();
    }

}