package info.rmarcus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

public class JsonArrayCollector implements Collector<JsonValue, ArrayList<JsonValue>, JsonArray> {
	
	
	@Override
	public BiConsumer<ArrayList<JsonValue>, JsonValue> accumulator() {
		return (currList, nextValue) -> {
			currList.add(nextValue);
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return new HashSet<>();
	}

	@Override
	public BinaryOperator<ArrayList<JsonValue>> combiner() {
		return (list1, list2) -> {
			list1.addAll(list2);
			return list1;
		};
	}

	@Override
	public Function<ArrayList<JsonValue>, JsonArray> finisher() {
		return (list) -> {
			JsonArray ja = new JsonArray();
			list.forEach(ja::add);
			return ja;
		};
	}

	@Override
	public Supplier<ArrayList<JsonValue>> supplier() {
		return () -> new ArrayList<JsonValue>();
	}


}
