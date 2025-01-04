package com.auroali.sanguinisluxuria.common.conversions.transformers;

import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.conversions.EntityConversionTransformer;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class CopyConversionTransformer implements EntityConversionTransformer {
    NbtTreeLocation srcPath;
    NbtTreeLocation dstPath;

    public CopyConversionTransformer(String srcPath, String dstPath) {
        this.srcPath = NbtTreeLocation.fromString(srcPath);
        this.dstPath = NbtTreeLocation.fromString(dstPath);
        if (this.dstPath == null)
            this.dstPath = this.srcPath;
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
        return new CopyConversionTransformer(srcPath, dstPath);
    }
}
