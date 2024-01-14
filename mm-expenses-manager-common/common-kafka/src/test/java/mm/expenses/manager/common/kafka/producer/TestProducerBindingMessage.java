package mm.expenses.manager.common.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TestProducerBindingMessage implements AsyncKafkaProducerBinding, Serializable {

    public static final String BINDING = "testBinding-out-0";
    public static final String TOPIC = "test-topic";

    private String value;

    private AsyncKafkaOperation operation;

    @Override
    public String getProducerBindingName() {
        return BINDING;
    }

    @Override
    public String getProducerTopicName() {
        return TOPIC;
    }

    public static TestProducerBindingMessage bindingNull() {
        return new TestProducerBindingMessage() {
            @Override
            public String getProducerBindingName() {
                return null;
            }
        };
    }

    public static TestProducerBindingMessage bindingEmptyString() {
        return new TestProducerBindingMessage() {
            @Override
            public String getProducerBindingName() {
                return StringUtils.EMPTY;
            }
        };
    }

    public static TestProducerBindingMessage topicNull() {
        return new TestProducerBindingMessage() {
            @Override
            public String getProducerTopicName() {
                return null;
            }
        };
    }

    public static TestProducerBindingMessage topicEmptyString() {
        return new TestProducerBindingMessage() {
            @Override
            public String getProducerTopicName() {
                return StringUtils.EMPTY;
            }
        };
    }

}
