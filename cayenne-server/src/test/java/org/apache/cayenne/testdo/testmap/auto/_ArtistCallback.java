package org.apache.cayenne.testdo.testmap.auto;

import java.util.Date;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

/**
 * Class _ArtistCallbackTest was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _ArtistCallback extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    @Deprecated
    public static final String ARTIST_NAME_PROPERTY = "artistName";
    @Deprecated
    public static final String DATE_OF_BIRTH_PROPERTY = "dateOfBirth";

    public static final String ARTIST_ID_PK_COLUMN = "ARTIST_ID";

    public static final Property<String> ARTIST_NAME = new Property<String>("artistName");
    public static final Property<Date> DATE_OF_BIRTH = new Property<Date>("dateOfBirth");

    public void setArtistName(String artistName) {
        writeProperty("artistName", artistName);
    }
    public String getArtistName() {
        return (String)readProperty("artistName");
    }

    public void setDateOfBirth(Date dateOfBirth) {
        writeProperty("dateOfBirth", dateOfBirth);
    }
    public Date getDateOfBirth() {
        return (Date)readProperty("dateOfBirth");
    }

    protected abstract void prePersistEntityObjEntity();

    protected abstract void preRemoveEntityObjEntity();

    protected abstract void preUpdateEntityObjEntity();

    protected abstract void postPersistEntityObjEntity();

    protected abstract void postRemoveEntityObjEntity();

    protected abstract void postUpdateEntityObjEntity();

    protected abstract void postLoadEntityObjEntity();

}