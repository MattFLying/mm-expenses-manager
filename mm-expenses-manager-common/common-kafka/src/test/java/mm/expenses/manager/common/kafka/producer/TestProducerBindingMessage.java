package mm.expenses.manager.common.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.async.AsyncBinding;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import org.apache.commons.lang3.StringUtils;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TestProducerBindingMessage implements AsyncBinding {

    public static final String BINDING = "testBinding-out-0";
    public static final String TOPIC = "test-topic";

    private String value;

    private AsyncKafkaOperation operation;

    @Override
    public String producerBindingName() {
        return BINDING;
    }

    @Override
    public String consumerBindingName() {
        return null;
    }

    @Override
    public String topicName() {
        return TOPIC;
    }

    public static TestProducerBindingMessage bindingNull() {
        return new TestProducerBindingMessage() {
            @Override
            public String producerBindingName() {
                return null;
            }
        };
    }

    public static TestProducerBindingMessage bindingEmptyString() {
        return new TestProducerBindingMessage() {
            @Override
            public String producerBindingName() {
                return StringUtils.EMPTY;
            }
        };
    }

    public static TestProducerBindingMessage topicNull() {
        return new TestProducerBindingMessage() {
            @Override
            public String topicName() {
                return null;
            }
        };
    }

    public static TestProducerBindingMessage topicEmptyString() {
        return new TestProducerBindingMessage() {
            @Override
            public String topicName() {
                return StringUtils.EMPTY;
            }
        };
    }

}
