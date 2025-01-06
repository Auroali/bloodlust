package com.auroali.sanguinisluxuria.common.conversions.transformers;

import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionTransformer;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class CopyConversionTransformer implements EntityConversionTransformer {
    final NbtTreeLocation srcPath;
    final NbtTreeLocation dstPath;

    public CopyConversionTransformer(NbtTreeLocation srcPath, NbtTreeLocation dstPath) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
    }

    @Override
    public void apply(ConversionContext context, NbtCompound nbtIn, NbtCompound nbtOut) {
        NbtElement element = this.srcPath.get(nbtIn);
        this.dstPath.insertInto(nbtOut, element);
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("src", this.srcPath.toString());
        if (!this.dstPath.equals(this.srcPath))
            object.addProperty("dst", this.dstPath.toString());
        return object;
    }

    @Override
    public Serializer<?> getSerializer() {
        return BLConversions.COPY_TRANSFORMER;
    }

    public static CopyConversionTransformer fromJson(JsonObject object) {
        String srcPath = object.get("src").getAsString();
        String dstPath = srcPath;
        if (object.has("dst"))
            dstPath = object.get("dst").getAsString();
        NbtTreeLocation src = NbtTreeLocation.fromString(srcPath);
        NbtTreeLocation dst = NbtTreeLocation.fromString(dstPath);
        if (src == null)
            throw new JsonParseException("Could not parse nbt tree location " + srcPath);
        return new CopyConversionTransformer(src, dst);
    }
}
