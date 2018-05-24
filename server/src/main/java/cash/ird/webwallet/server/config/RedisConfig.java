package cash.ird.webwallet.server.config;

import cash.ird.webwallet.server.config.converter.BytesToUserConverter;
import cash.ird.webwallet.server.config.converter.UserToBytesConverter;
import cash.ird.webwallet.server.domain.User;
import cash.ird.webwallet.server.domain.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.convert.CustomConversions;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Bean("redisConnectionFactory")
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    @Autowired
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

//    @Bean("reactiveRedisConnectionFactory")
//    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
//        return new LettuceConnectionFactory();
//    }
//
//    @Bean
//    public ReactiveRedisConnection reactiveRedisConnection(final ReactiveRedisConnectionFactory redisConnectionFactory) {
//        return redisConnectionFactory.getReactiveConnection();
//    }
//
//    @Bean
//    @Autowired
//    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(@Qualifier("reactiveRedisConnectionFactory") ReactiveRedisConnectionFactory factory) {
//        ReactiveRedisTemplate<String, String> template = new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
//        return template;
//    }
//
//    @Bean
//    public RedisTemplate<?, ?> redisTemplate() {
//        return new RedisTemplate<byte[], byte[]>();
//    }
//
//    @Bean
//    public RedisCustomConversions redisCustomConversions(){
//
//        return new RedisCustomConversions(Arrays.asList(
//                new BytesToUserConverter(), new UserToBytesConverter()
//        ));
//    }
//
//    @Bean("redisTemplate")
//    @Autowired
//    public RedisTemplate<Object, Object> redisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory factory) {
//        final RedisTemplate<Object, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        template.setStringSerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//        return template;
//    }

//    @Bean("redisObjectTemplate")
//    @Autowired
//    public RedisTemplate<String, Object> redisObjectTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory factory) {
//        final RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        return template;
//    }
//
//    @Bean("redisUserTemplate")
//    @Autowired
//    public RedisTemplate<String, User> redisUserTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory factory) {
//        final RedisTemplate<String, User> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(User.class));
//        return template;
//    }
//
//    @Bean("redisWalletTemplate")
//    @Autowired
//    public RedisTemplate<String, Wallet> redisWalletTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory factory) {
//        final RedisTemplate<String, Wallet> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Wallet.class));
//        return template;
//    }
//
//    @Bean("redisStringTemplate")
//    @Autowired
//    public RedisTemplate<String, String> redisStringTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory factory) {
//        return new StringRedisTemplate(factory);
//    }

}