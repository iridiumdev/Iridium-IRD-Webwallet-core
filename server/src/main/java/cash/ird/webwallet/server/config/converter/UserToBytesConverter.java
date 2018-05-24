package cash.ird.webwallet.server.config.converter;

import cash.ird.webwallet.server.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@WritingConverter
public class UserToBytesConverter implements Converter<User, byte[]> {

  private final RedisSerializer<User> serializer;

  public UserToBytesConverter() {
    serializer = new Jackson2JsonRedisSerializer<>(User.class);
    ((Jackson2JsonRedisSerializer<User>) serializer).setObjectMapper(new ObjectMapper());
  }

  @Override
  public byte[] convert(User value) {
    return serializer.serialize(value);
  }
}

