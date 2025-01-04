package com.auroali.sanguinisluxuria.common.conversions.transformers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Arrays;

public class NbtTreeLocation {
    String[] nodes;

    private NbtTreeLocation(String[] pathEntries) {
        this.nodes = pathEntries;
    }

    public static NbtTreeLocation fromString(String path) {
        String[] nodes = path.split("\\.");
        if (nodes.length == 0)
            return null;
        return new NbtTreeLocation(nodes);
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
        return this == obj || obj instanceof NbtTreeLocation path && Arrays.equals(this.nodes, path.nodes);
    }
}
