package io.eagle.util;

import com.google.common.collect.Sets;


/**
 * Determines behavior when writing new records, including when the records have identical timestamps as existing records.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public enum WriteMode {
    /**
     * Merge new records with existing records; on colliding timestamps, preserve existing records
     */
    MERGE_PRESERVE_EXISTING,

    /**
     * Merge new records with existing records; on colliding timestamps, overwrite existing records
     */
    MERGE_OVERWRITE_EXISTING,

    /**
     * Merge new records by overwriting field attributes with supplied sparse fields, also includes $delete operator
     */
    MERGE_UPDATE_EXISTING,

    /**
     * Merge new records with existing records; on colliding timestamps, fail the operation without modification
     */
    MERGE_FAIL_ON_EXISTING,

    /**
     * First delete any existing records falling within the period defined by the new records, then insert the new records
     */
    INSERT_DELETE_EXISTING,

    /**
     * If there are any existing records falling within the period defined by the new records, fail the operation without modification;
     * otherwise insert the new records
     */
    INSERT_FAIL_ON_EXISTING,

    /**
     * Delete records encapsulated by the given range; inclusive of start and end
     */
    DELETE_RANGE,

    /**
     * Delete specific records
     */
    DELETE,

    /**
     * Discard all records
     */
    DISCARD;

    public boolean isDiscard() {
        return Sets.newHashSet(DISCARD).contains(this);
    }

    public boolean isDelete() {
        return Sets.newHashSet(DELETE, DELETE_RANGE, MERGE_UPDATE_EXISTING).contains(this);
    }


    public boolean isOverwrite() {
        return Sets.newHashSet(MERGE_OVERWRITE_EXISTING, INSERT_DELETE_EXISTING, MERGE_UPDATE_EXISTING).contains(this);
    }


    public boolean isPreserve() {
        return Sets.newHashSet(MERGE_PRESERVE_EXISTING, INSERT_FAIL_ON_EXISTING, MERGE_FAIL_ON_EXISTING).contains(this);
    }

}
