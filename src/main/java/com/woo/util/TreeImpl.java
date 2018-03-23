package com.woo.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TreeImpl<E, ID>
        implements Tree<E, ID>
{
    public static final String PATH_SEP = ".";
    private ID id;
    private E entity;
    private List<Tree<E, ID>> children = new ArrayList();
    private Tree<E, ID> parent;
    private boolean isLeaf = true;

    public TreeImpl(E entity, ID id)
    {
        if (id == null) {
            throw new NullPointerException();
        }
        this.entity = entity;
        this.id = id;
    }

    public List<Tree<E, ID>> getChildren()
    {
        return this.children;
    }

    public List<Tree<E, ID>> getTrees()
    {
        List res = new ArrayList();
        for (Tree t : this.children)
            if (!t.isLeaf())
                res.add(t);
        return res;
    }

    public List<E> getLeaves()
    {
        List res = new ArrayList();
        for (Tree t : this.children)
            if (t.isLeaf())
                res.add(t.getEntity());
        return res;
    }

    public Tree<E, ID> locate(ID[] pathes)
    {
        Tree current = this;
        boolean found = false;
        for (Object path : pathes) {
            found = false;
            for (Tree tree : current.getChildren()) {
                if (tree.getId().equals(path)) {
                    current = tree;
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;
        }
        return current;
    }

    public Tree<E, ID> mkdirs(ID[] pathes)
    {
        Tree current = this;
        boolean found = false;
        for (Object path : pathes) {
            found = false;
            for (Tree tree : current.getChildren()) {
                if (tree.getId().equals(path)) {
                    current = tree;
                    found = true;
                    break;
                }
            }
            if (!found) {
                Tree newTree = new TreeImpl(null, path);
                current.addChild(newTree);
                current = newTree;
            }
        }
        return current;
    }

    public boolean addChild(ID[] parentPathes, Tree<E, ID> childTree, boolean mkdir)
    {
        if (!mkdir) {
            return addChild(parentPathes, childTree);
        }
        Tree parent = mkdirs(parentPathes);
        parent.addChild(childTree);
        return true;
    }

    private boolean addChild(ID[] parentPathes, Tree<E, ID> childTree)
    {
        Tree parent = locate(parentPathes);
        if (parent == null)
            return false;
        parent.addChild(childTree);
        return true;
    }

    public void addChild(Tree<E, ID> childTree)
    {
        childTree.setParent(this);
        this.children.add(childTree);
        this.isLeaf = false;
    }

    public void addEntity(E leafEntity, ID name)
    {
        addChild(new TreeImpl(leafEntity, name));
    }

    public boolean removeChild(Tree<E, ID> childTree)
    {
        boolean res = this.children.remove(childTree);
        this.isLeaf = this.children.isEmpty();
        return res;
    }

    public Tree<E, ID> removeEntity(E leafEntity)
    {
        Tree firstMatchChild = null;
        for (Tree tree : this.children) {
            if (((tree.getEntity() == null) && (leafEntity == null)) || (tree.getEntity().equals(leafEntity))) {
                firstMatchChild = tree;
                break;
            }
        }
        if (firstMatchChild != null) {
            removeChild(firstMatchChild);
        }
        return firstMatchChild;
    }

    public E removeEntity(ID id)
    {
        Tree firstMatchChild = null;
        for (Tree tree : this.children) {
            if (tree.getId().equals(id)) {
                firstMatchChild = tree;
                break;
            }
        }
        if (firstMatchChild != null) {
            removeChild(firstMatchChild);
        }
        return firstMatchChild.getEntity();
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof Tree))
            return false;
        Tree tree = (Tree)obj;

        return (this.id.equals(tree.getId())) && (
                ((this.entity == null) && (tree.getEntity() == null)) || (this.entity.equals(tree.getEntity())));
    }

    public int hashCode()
    {
        if (this.entity == null)
            return this.id.hashCode();
        return this.entity.hashCode();
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        toTextTree(sb, this, 0);
        return sb.toString();
    }

    private void toTextTree(StringBuilder sb, Tree<E, ID> tree, int deepth)
    {
        sb.append(super.toString());
    }

    public E getEntity()
    {
        return this.entity;
    }

    public void setEntity(E entity)
    {
        this.entity = entity;
    }

    public Tree<E, ID> getParent()
    {
        return this.parent;
    }

    public void setParent(Tree<E, ID> parent)
    {
        this.parent = parent;
    }

    public ID getId()
    {
        return this.id;
    }

    public boolean isLeaf()
    {
        return this.isLeaf;
    }

    public static void main(String[] args)
    {
        System.out.println(System.getProperties());
        System.out.println("java.compiler=" + System.getProperty("java.compiler"));
    }
}