package com.auroali.sanguinisluxuria.common.conversions.transformers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Arrays;

public class NbtTreeLocation {
    final String[] nodes;

    private NbtTreeLocation(String[] pathEntries) {
        this.nodes = pathEntries;
    }

    public static NbtTreeLocation fromString(String path) {
        String[] nodes = path.split("\\.");
        if (nodes.length == 0)
            return null;
        return new NbtTreeLocation(nodes);
    }

    protected NbtCompound getParent(NbtCompound tag) {
        NbtCompound parent = tag;
        for (int i = 0; i < this.nodes.length - 1; i++) {
            NbtElement element = tag.get(this.nodes[i]);
            if (element == null || element.getType() != NbtElement.COMPOUND_TYPE)
                return null;
            parent = (NbtCompound) element;
        }
        return parent;
    }

    public NbtElement get(NbtCompound tag) {
        NbtCompound parent = this.getParent(tag);
        if (parent == null)
            return null;
        return parent.get(this.nodes[this.nodes.length - 1]);
    }

    public void insertInto(NbtCompound tag, NbtElement element) {
        NbtCompound parent = this.getParent(tag);
        if (parent != null)
            parent.put(this.nodes[this.nodes.length - 1], element);
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
