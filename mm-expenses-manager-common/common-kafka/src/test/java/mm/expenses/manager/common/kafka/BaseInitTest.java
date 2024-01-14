package mm.expenses.manager.common.kafka;

import mm.expenses.manager.common.kafka.producer.AsyncKafkaProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.mockito.InjectMocks;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

public class BaseInitTest {

    protected StreamBridge streamBridge = mock(StreamBridge.class);

    @InjectMocks
    protected AsyncKafkaProducer producer;

    @BeforeEach
    @AfterEach
    protected void beforeEachTest() {
        reset(streamBridge);
    }

    public static class AsyncKafkaOperationArgument implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(AsyncKafkaOperation.values()).map(Arguments::of);
        }
    }

}
