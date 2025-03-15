package mm.expenses.manager.common.postgresql.specification;

import lombok.val;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class FieldTypeArgument implements ArgumentsProvider {

    @Override
    public Stream<Arguments> provideArguments(final ExtensionContext context) {
        return Stream.of(FieldType.values()).map(Arguments::of);
    }

    public static class FieldTypeWithAllOperationsArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val fieldTypes = Arrays.asList(FieldType.values());
            val operations = Arrays.asList(Operation.values());

            val result = new ArrayList<Arguments>();
            fieldTypes.forEach(fieldType -> {
                operations.forEach(operation -> result.add(Arguments.of(fieldType, operation)));
            });
            return result.stream();
        }

    }

    public static class FieldTypeWithAvailableIsNullOperationArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val fieldTypes = Arrays.asList(FieldType.values());
            val operations = Arrays.asList(Operation.values());

            val result = new ArrayList<Arguments>();
            fieldTypes.forEach(fieldType -> {
                operations.forEach(operation -> {
                    if (!Operation.isNull.equals(operation)) {
                        return;
                    }
                    if (fieldType.equals(FieldType.Boolean)) {
                        return;
                    }
                    result.add(Arguments.of(fieldType, operation));
                });
            });
            return result.stream();
        }

    }

    public static class FieldTypeListWithAvailableOperationsArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val fieldTypes = Arrays.asList(FieldType.values());
            val operations = Arrays.asList(Operation.values());

            val result = new ArrayList<Arguments>();
            fieldTypes.forEach(fieldType -> {
                operations.forEach(operation -> {
                    if (!fieldType.equals(FieldType.List)) {
                        return;
                    }
                    if (operation.isIn(Operation.startsWith, Operation.endsWith, Operation.contains, Operation.isNull)) {
                        return;
                    }
                    result.add(Arguments.of(fieldType, operation));
                });
            });
            return result.stream();
        }

    }

    public static class FieldTypeListWithEqualOperationsArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val fieldTypes = Arrays.asList(FieldType.values());
            val operations = Arrays.asList(Operation.values());

            val result = new ArrayList<Arguments>();
            fieldTypes.forEach(fieldType -> {
                operations.forEach(operation -> {
                    if (!fieldType.equals(FieldType.String)) {
                        return;
                    }
                    if (operation.isIn(Operation.equal, Operation.notEqual, Operation.isNull)) {
                        return;
                    }
                    result.add(Arguments.of(fieldType, operation));
                });
            });
            return result.stream();
        }

    }

    public static class FieldTypeWithAvailableOperationsArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val fieldTypes = Arrays.asList(FieldType.values());
            val operations = Arrays.asList(Operation.values());

            val result = new ArrayList<Arguments>();
            fieldTypes.forEach(fieldType -> {
                operations.forEach(operation -> {
                    if (fieldType.equals(FieldType.Boolean) && !Operation.equal.equals(operation)) {
                        return;
                    }
                    if (List.of(FieldType.Long, FieldType.Integer, FieldType.Instant, FieldType.List, FieldType.Object).contains(fieldType) && operation.isIn(Operation.startsWith, Operation.endsWith, Operation.contains)) {
                        return;
                    }
                    result.add(Arguments.of(fieldType, operation));
                });
            });
            return result.stream();
        }

    }

    public static class FieldTypeDifferentThanStringWithStringSpecificOperationsArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val fieldTypes = Arrays.asList(FieldType.values());
            val operations = Arrays.asList(Operation.values());

            val result = new ArrayList<Arguments>();
            fieldTypes.forEach(fieldType -> {
                operations.forEach(operation -> {
                    if (fieldType.equals(FieldType.String) || fieldType.equals(FieldType.Boolean) || fieldType.equals(FieldType.JsonB)) {
                        return;
                    }
                    if (!operation.isIn(Operation.startsWith, Operation.endsWith, Operation.contains)) {
                        return;
                    }
                    result.add(Arguments.of(fieldType, operation));
                });
            });
            return result.stream();
        }

    }

}
