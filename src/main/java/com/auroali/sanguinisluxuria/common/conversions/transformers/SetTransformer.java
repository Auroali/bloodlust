package com.auroali.sanguinisluxuria.common.conversions.transformers;

import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionTransformer;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class SetTransformer implements EntityConversionTransformer {
    final NbtTreeLocation dst;
    final NbtElement element;

    public SetTransformer(NbtTreeLocation dst, NbtElement element) {
        this.dst = dst;
        this.element = element;
    }

    @Override
    public void apply(ConversionContext context, NbtCompound nbtIn, NbtCompound nbtOut) {
        this.dst.insertInto(nbtOut, this.element);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        nbtToJson(this.element, object);
        object.addProperty("dst", this.dst.toString());
        return object;
    }

    @Override
    public Serializer<?> getSerializer() {
        return BLConversions.SET_TRANSFORMER;
    }

    public static SetTransformer fromJson(JsonObject object) {
        NbtElement element = nbtFromJson(object);
        NbtTreeLocation dst = NbtTreeLocation.fromString(object.get("dst").getAsString());
        if (dst == null)
            throw new JsonParseException("Failed to parse nbt tree location");
        return new SetTransformer(dst, element);
    }

    public static SetTransformer create(String dst, NbtElement element) {
        return new SetTransformer(NbtTreeLocation.fromString(dst), element);
    }

    public static SetTransformer create(String dst, int element) {
        return new SetTransformer(NbtTreeLocation.fromString(dst), NbtInt.of(element));
    }

    public static SetTransformer create(String dst, long element) {
        return new SetTransformer(NbtTreeLocation.fromString(dst), NbtLong.of(element));
    }

    public static SetTransformer create(String dst, byte element) {
        return new SetTransformer(NbtTreeLocation.fromString(dst), NbtByte.of(element));
    }

    public static SetTransformer create(String dst, short element) {
        return new SetTransformer(NbtTreeLocation.fromString(dst), NbtShort.of(element));
    }

    public static SetTransformer create(String dst, String element) {
        return new SetTransformer(NbtTreeLocation.fromString(dst), NbtString.of(element));
    }

    public static EntityConversionTransformer create(String dst, float element) {
        return new SetTransformer(NbtTreeLocation.fromString(dst), NbtFloat.of(element));
    }

    public static EntityConversionTransformer create(String dst, double element) {
        return new SetTransformer(NbtTreeLocation.fromString(dst), NbtDouble.of(element));
    }


    protected static NbtElement nbtFromJson(JsonObject object) {
        String type = object.get("nbtType").getAsString();
        JsonElement value = object.get("nbtValue");
        return switch (type) {
            case "long" -> NbtLong.of(value.getAsLong());
            case "int" -> NbtInt.of(value.getAsInt());
            case "short" -> NbtShort.of(value.getAsShort());
            case "byte" -> NbtByte.of(value.getAsByte());
            case "float" -> NbtFloat.of(value.getAsFloat());
            case "double" -> NbtDouble.of(value.getAsDouble());
            case "string" -> NbtString.of(value.getAsString());
            case "uuid" -> NbtHelper.fromUuid(UUID.fromString(value.getAsString()));
            case "compound" -> compoundFromJson(value);
            case "list" -> listFromJson(value);
            case "bytearray" -> arrayFromList(value, JsonElement::getAsByte, NbtByteArray::new);
            case "intarray" -> arrayFromList(value, JsonElement::getAsInt, NbtIntArray::new);
            case "longarray" -> arrayFromList(value, JsonElement::getAsLong, NbtLongArray::new);
            default -> throw new JsonParseException("Unknown type " + type);
        };
    }

    protected static NbtList listFromJson(JsonElement element) {
        if (!element.isJsonArray())
            throw new JsonParseException("Expected json array but got " + element);
        NbtList list = new NbtList();
        for (JsonElement value : element.getAsJsonArray()) {
            if (!value.isJsonObject())
                throw new JsonParseException("Expected json object but got " + element);

            NbtElement nbt = nbtFromJson(value.getAsJsonObject());
            list.add(nbt);
        }

        return list;
    }

    protected static JsonElement listToJson(NbtList list) {
        JsonArray array = new JsonArray();
        for (NbtElement element : list) {
            JsonObject object = new JsonObject();
            nbtToJson(element, object);
            array.add(object);
        }
        return array;
    }

    protected static <T> AbstractNbtList<?> arrayFromList(JsonElement element, Function<JsonElement, T> converter, Function<List<T>, AbstractNbtList<?>> factory) {
        if (!element.isJsonArray())
            throw new JsonParseException("Expected json array but got " + element);

        JsonArray array = element.getAsJsonArray();
        List<T> values = new ArrayList<>();
        for (JsonElement value : array) {
            T v = converter.apply(value);
            values.add(v);
        }

        return factory.apply(values);
    }

    protected static <T, U extends NbtElement> JsonElement writeArrayToJson(AbstractNbtList<U> list, Function<U, T> converter, BiConsumer<JsonArray, T> adder) {
        JsonArray array = new JsonArray();
        for (U element : list) {
            adder.accept(array, converter.apply(element));
        }
        return array;
    }

    protected static NbtCompound compoundFromJson(JsonElement element) {
        if (!element.isJsonObject())
            throw new JsonParseException("Expected json object for compound type but got " + element);

        JsonObject object = element.getAsJsonObject();
        NbtCompound tag = new NbtCompound();
        for (Map.Entry<String, JsonElement> entry : object.asMap().entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (!value.isJsonObject())
                throw new JsonParseException("Expected json object but got " + value);
            tag.put(key, nbtFromJson(value.getAsJsonObject()));
        }

        return tag;
    }

    protected static JsonElement compoundToJson(NbtCompound tag) {
        JsonObject object = new JsonObject();
        for (String key : tag.getKeys()) {
            NbtElement element = tag.get(key);
            JsonObject jsonElement = new JsonObject();
            nbtToJson(element, jsonElement);
            object.add(key, jsonElement);
        }

        return object;
    }

    protected static void nbtToJson(NbtElement element, JsonObject object) {
        switch (element.getType()) {
            case NbtElement.LONG_TYPE ->
              withType(object, "long", obj -> obj.addProperty("nbtValue", ((AbstractNbtNumber) element).longValue()));
            case NbtElement.INT_TYPE ->
              withType(object, "int", obj -> obj.addProperty("nbtValue", ((AbstractNbtNumber) element).intValue()));
            case NbtElement.SHORT_TYPE ->
              withType(object, "short", obj -> obj.addProperty("nbtValue", ((AbstractNbtNumber) element).shortValue()));
            case NbtElement.BYTE_TYPE ->
              withType(object, "byte", obj -> obj.addProperty("nbtValue", ((AbstractNbtNumber) element).byteValue()));
            case NbtElement.FLOAT_TYPE ->
              withType(object, "float", obj -> obj.addProperty("nbtValue", ((AbstractNbtNumber) element).floatValue()));
            case NbtElement.DOUBLE_TYPE ->
              withType(object, "double", obj -> obj.addProperty("nbtValue", ((AbstractNbtNumber) element).doubleValue()));
            case NbtElement.STRING_TYPE ->
              withType(object, "string", obj -> obj.addProperty("nbtValue", element.asString()));
            case NbtElement.COMPOUND_TYPE ->
              withType(object, "compound", obj -> obj.add("nbtValue", compoundToJson((NbtCompound) element)));
            case NbtElement.LIST_TYPE ->
              withType(object, "list", obj -> obj.add("nbtValue", listToJson((NbtList) element)));
            case NbtElement.BYTE_ARRAY_TYPE ->
              withType(object, "bytearray", obj -> obj.add("nbtValue", writeArrayToJson((NbtByteArray) element, AbstractNbtNumber::byteValue, JsonArray::add)));
            case NbtElement.INT_ARRAY_TYPE ->
              withType(object, "intarray", obj -> obj.add("nbtValue", writeArrayToJson((NbtIntArray) element, AbstractNbtNumber::intValue, JsonArray::add)));
            case NbtElement.LONG_ARRAY_TYPE ->
              withType(object, "longarray", obj -> obj.add("nbtValue", writeArrayToJson((NbtLongArray) element, AbstractNbtNumber::longValue, JsonArray::add)));
            default -> throw new JsonParseException("Unknown nbt type " + element.getType());
        }
    }

    protected static void withType(JsonObject object, String type, Consumer<JsonObject> consumer) {
        object.addProperty("nbtType", type);
        consumer.accept(object);
    }
}
