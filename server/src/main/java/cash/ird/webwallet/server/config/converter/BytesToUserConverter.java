package cash.ird.webwallet.server.config.converter;

import cash.ird.webwallet.server.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@ReadingConverter
public class BytesToUserConverter implements Converter<byte[], User> {

  private final RedisSerializer<User> serializer;

  public BytesToUserConverter() {

    serializer = new Jackson2JsonRedisSerializer<>(User.class);
    ((Jackson2JsonRedisSerializer<User>) serializer).setObjectMapper(new ObjectMapper());
  }

  @Override
  public User convert(byte[] value) {
    return serializer.deserialize(value);
  }
}