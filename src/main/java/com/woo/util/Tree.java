package com.woo.util;

import java.util.List;

public abstract interface Tree<E, ID>
{
    public static final String PATH_SEP_DOT = ".";

    public abstract List<Tree<E, ID>> getChildren();

    public abstract List<Tree<E, ID>> getTrees();

    public abstract List<E> getLeaves();

    public abstract boolean isLeaf();

    public abstract Tree<E, ID> getParent();

    public abstract void setParent(Tree<E, ID> tree);

    public abstract E getEntity();

    public abstract void setEntity(E e);

    public abstract ID getId();

    public abstract Tree<E, ID> mkdirs(ID[] ids);

    public abstract boolean addChild(ID[] ids, Tree<E, ID> tree, boolean mkdir);

    public abstract void addChild(Tree<E, ID> tree);

    public abstract boolean removeChild(Tree<E, ID> tree);

    public abstract void addEntity(E paramE, ID id);

    public abstract Tree<E, ID> removeEntity(E e);

    public abstract E removeEntity(ID id);

    public abstract Tree<E, ID> locate(ID[] ids);
}