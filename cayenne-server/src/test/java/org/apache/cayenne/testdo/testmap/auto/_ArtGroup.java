package org.apache.cayenne.testdo.testmap.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.EntityProperty;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.NumericIdProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.StringProperty;
import org.apache.cayenne.testdo.testmap.ArtGroup;
import org.apache.cayenne.testdo.testmap.Artist;

/**
 * Class _ArtGroup was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _ArtGroup extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final NumericIdProperty<Integer> GROUP_ID_PK_PROPERTY = PropertyFactory.createNumericId("GROUP_ID", "ArtGroup", Integer.class);
    public static final String GROUP_ID_PK_COLUMN = "GROUP_ID";

    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final ListProperty<Artist> ARTIST_ARRAY = PropertyFactory.createList("artistArray", Artist.class);
    public static final ListProperty<ArtGroup> CHILD_GROUPS_ARRAY = PropertyFactory.createList("childGroupsArray", ArtGroup.class);
    public static final EntityProperty<ArtGroup> TO_PARENT_GROUP = PropertyFactory.createEntity("toParentGroup", ArtGroup.class);

    protected String name;

    protected Object artistArray;
    protected Object childGroupsArray;
    protected Object toParentGroup;

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void addToArtistArray(Artist obj) {
        addToManyTarget("artistArray", obj, true);
    }

    public void removeFromArtistArray(Artist obj) {
        removeToManyTarget("artistArray", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<Artist> getArtistArray() {
        return (List<Artist>)readProperty("artistArray");
    }

    public void addToChildGroupsArray(ArtGroup obj) {
        addToManyTarget("childGroupsArray", obj, true);
    }

    public void removeFromChildGroupsArray(ArtGroup obj) {
        removeToManyTarget("childGroupsArray", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<ArtGroup> getChildGroupsArray() {
        return (List<ArtGroup>)readProperty("childGroupsArray");
    }

    public void setToParentGroup(ArtGroup toParentGroup) {
        setToOneTarget("toParentGroup", toParentGroup, true);
    }

    public ArtGroup getToParentGroup() {
        return (ArtGroup)readProperty("toParentGroup");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "name":
                return this.name;
            case "artistArray":
                return this.artistArray;
            case "childGroupsArray":
                return this.childGroupsArray;
            case "toParentGroup":
                return this.toParentGroup;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "name":
                this.name = (String)val;
                break;
            case "artistArray":
                this.artistArray = val;
                break;
            case "childGroupsArray":
                this.childGroupsArray = val;
                break;
            case "toParentGroup":
                this.toParentGroup = val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.name);
        out.writeObject(this.artistArray);
        out.writeObject(this.childGroupsArray);
        out.writeObject(this.toParentGroup);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.name = (String)in.readObject();
        this.artistArray = in.readObject();
        this.childGroupsArray = in.readObject();
        this.toParentGroup = in.readObject();
    }

}
