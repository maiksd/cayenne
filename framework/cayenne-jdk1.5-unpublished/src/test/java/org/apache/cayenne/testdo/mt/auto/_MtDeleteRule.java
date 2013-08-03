package org.apache.cayenne.testdo.mt.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.mt.MtDeleteCascade;
import org.apache.cayenne.testdo.mt.MtDeleteDeny;
import org.apache.cayenne.testdo.mt.MtDeleteNullify;

/**
 * Class _MtDeleteRule was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _MtDeleteRule extends CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String FROM_CASCADE_PROPERTY = "fromCascade";
    public static final String FROM_DENY_PROPERTY = "fromDeny";
    public static final String FROM_NULLIFY_PROPERTY = "fromNullify";

    public static final String DELETE_RULE_ID_PK_COLUMN = "DELETE_RULE_ID";

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void addToFromCascade(MtDeleteCascade obj) {
        addToManyTarget("fromCascade", obj, true);
    }
    public void removeFromFromCascade(MtDeleteCascade obj) {
        removeToManyTarget("fromCascade", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<MtDeleteCascade> getFromCascade() {
        return (List<MtDeleteCascade>)readProperty("fromCascade");
    }


    public void addToFromDeny(MtDeleteDeny obj) {
        addToManyTarget("fromDeny", obj, true);
    }
    public void removeFromFromDeny(MtDeleteDeny obj) {
        removeToManyTarget("fromDeny", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<MtDeleteDeny> getFromDeny() {
        return (List<MtDeleteDeny>)readProperty("fromDeny");
    }


    public void addToFromNullify(MtDeleteNullify obj) {
        addToManyTarget("fromNullify", obj, true);
    }
    public void removeFromFromNullify(MtDeleteNullify obj) {
        removeToManyTarget("fromNullify", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<MtDeleteNullify> getFromNullify() {
        return (List<MtDeleteNullify>)readProperty("fromNullify");
    }


}