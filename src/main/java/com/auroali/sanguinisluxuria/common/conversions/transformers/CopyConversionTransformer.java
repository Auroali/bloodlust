package com.auroali.sanguinisluxuria.common.conversions.transformers;

import com.auroali.sanguinisluxuria.common.conversions.EntityConversionTransformer;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.util.dynamic.Codecs;

import java.util.Arrays;
import java.util.List;

public class CopyConversionTransformer implements EntityConversionTransformer {
    NbtPath srcPath;
    NbtPath dstPath;

    public CopyConversionTransformer(String srcPath, String dstPath) {
        this.srcPath = NbtPath.fromString(srcPath);
        this.dstPath = NbtPath.fromString(dstPath);
        if (this.dstPath == null)
            this.dstPath = this.srcPath;
    }

    @Override
    public void apply(Entity entity, NbtCompound nbtIn, NbtCompound nbtOut) {
        NbtElement element = this.srcPath.get(nbtIn);
        this.dstPath.insertInto(nbtOut, element);
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("src", this.srcPath.toString());
        if (!this.dstPath.equals(this.srcPath))
            object.addProperty("dst", this.dstPath.toString());
        return object;
    }

    @Override
    public Serializer<?> getSerializer() {
        return BLConversions.COPY;
    }

    public static CopyConversionTransformer fromJson(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        String srcPath = object.get("src").getAsString();
        String dstPath = srcPath;
        if (object.has("dst"))
            dstPath = object.get("dst").getAsString();
        return new CopyConversionTransformer(srcPath, dstPath);
    }

    private static class NbtPath {
        String[] nodes;

        private NbtPath(String[] pathEntries) {
            this.nodes = pathEntries;
        }

        public static NbtPath fromString(String path) {
            String[] nodes = path.split("\\.");
            if (nodes.length == 0)
                return null;
            return new NbtPath(nodes);
        }

        public NbtElement get(NbtCompound tag) {
            NbtElement element = null;
            for (int i = 0; i < this.nodes.length; i++) {
                element = tag.get(this.nodes[i]);
                if (element == null)
                    return null;
                // if this isnt the last index and it isn't a compound tag, return null
                if (i != this.nodes.length - 1 && element.getType() != NbtElement.COMPOUND_TYPE)
                    return null;
            }
            return element;
        }

        public void insertInto(NbtCompound tag, NbtElement element) {
            NbtElement found = null;
            for (int i = 0; i < this.nodes.length; i++) {
                found = tag.get(this.nodes[i]);
                if (found == null)
                    return;
                // unlike get, this checks the second to last index
                // as this stops at the parent
                if (i != this.nodes.length - 2 && found.getType() != NbtElement.COMPOUND_TYPE)
                    return;
            }

            if (found != null && found.getType() == NbtElement.COMPOUND_TYPE)
                ((NbtCompound) found).put(this.nodes[this.nodes.length - 1], element);
        }

        @Override
        public String toString() {
            return String.join(".", this.nodes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.nodes);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj instanceof NbtPath path && Arrays.equals(this.nodes, path.nodes);
        }
    }
}
