package mm.expenses.manager.common.async;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncConsumerBindingTest implements AsyncBinding {

    public static final String BINDING = "async-consumer-test-in-0";
    public static final String TOPIC = "async-consumer-test";

    private String value;

    @Override
    public String producerBindingName() {
        return "";
    }

    @Override
    public String consumerBindingName() {
        return BINDING;
    }

    @Override
    public String topicName() {
        return TOPIC;
    }

    public static AsyncConsumerBindingTest bindingNull() {
        return new AsyncConsumerBindingTest() {
            @Override
            public String topicName() {
                return null;
            }
        };
    }

    public static AsyncConsumerBindingTest topicNull() {
        return new AsyncConsumerBindingTest() {
            @Override
            public String topicName() {
                return null;
            }
        };
    }

}
