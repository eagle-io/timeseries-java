package io.eagle.util.jts;

import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;


public class SeriesChange {

    public static final SeriesChange NO_CHANGE = new SeriesChange();

    private Integer insertedRecords = 0;
    private Integer insertedValues = 0;
    private Integer insertedQuality = 0;
    private Integer insertedAnnotations = 0;
    private Integer modifiedRecords = 0;
    private Integer modifiedValues = 0;
    private Integer modifiedQuality = 0;
    private Integer modifiedAnnotations = 0;
    private Integer deletedRecords = 0;


    public SeriesChange() {
        this.insertedRecords = 0;
        this.insertedValues = 0;
        this.insertedQuality = 0;
        this.insertedAnnotations = 0;
        this.modifiedRecords = 0;
        this.modifiedValues = 0;
        this.modifiedQuality = 0;
        this.modifiedAnnotations = 0;
        this.deletedRecords = 0;
    }


    public SeriesChange(SeriesChange other) {
        this.insertedRecords = other.insertedRecords;
        this.insertedValues = other.insertedValues;
        this.insertedQuality = other.insertedQuality;
        this.insertedAnnotations = other.insertedAnnotations;
        this.modifiedRecords = other.modifiedRecords;
        this.modifiedValues = other.modifiedValues;
        this.modifiedQuality = other.modifiedQuality;
        this.modifiedAnnotations = other.modifiedAnnotations;
        this.deletedRecords = other.deletedRecords;

    }


    public static SeriesChange inserted(Integer inserted) {
        SeriesChange change = new SeriesChange();
        change.insertedRecords = inserted;
        return change;
    }


    public static SeriesChange modified(Integer modified) {
        SeriesChange change = new SeriesChange();
        change.modifiedRecords = modified;
        return change;
    }


    public static SeriesChange deleted(Integer deleted) {
        SeriesChange change = new SeriesChange();
        change.deletedRecords = deleted;
        return change;
    }


    public boolean hasChanged() {
        return !this.equals(NO_CHANGE);
    }


    /**
     * 457 records inserted [776 value(s)], 6 modified [3 value(s), 6 quality, 1 annotation(s)], 5 deleted
     */
    @Override
    public String toString() {
        LinkedHashMap<String, Integer> recordChanges = Maps.newLinkedHashMap();

        if (this.insertedRecords > 0) {
            Map<String, Integer> insertedChanges = Maps.newLinkedHashMap();

            if (this.insertedValues > 1)
                insertedChanges.put("values", this.insertedValues);
            else if (this.insertedValues > 0)
                insertedChanges.put("value", 1);

            if (this.insertedQuality > 0)
                insertedChanges.put("quality", this.insertedQuality);

            if (this.insertedAnnotations > 1)
                insertedChanges.put("annotations", this.insertedAnnotations);
            else if (this.insertedAnnotations > 0)
                insertedChanges.put("annotation", 1);

            String summary = insertedChanges.isEmpty() ? ""
                    : insertedChanges.entrySet().stream()
                    .map(e -> e.getValue() + " " + e.getKey())
                    .collect(Collectors.joining(", ", " [", "]"));
            recordChanges.put(String.format("inserted%s", summary), this.insertedRecords);
        }

        if (this.modifiedRecords > 0) {
            Map<String, Integer> modifiedChanges = Maps.newLinkedHashMap();

            if (this.modifiedValues > 1)
                modifiedChanges.put("values", this.modifiedValues);
            else if (this.insertedValues > 0)
                modifiedChanges.put("value", 1);

            if (this.modifiedQuality > 0)
                modifiedChanges.put("quality", this.modifiedQuality);

            if (this.modifiedAnnotations > 1)
                modifiedChanges.put("annotations", this.modifiedAnnotations);
            else if (this.modifiedAnnotations > 0)
                modifiedChanges.put("annotation", 1);

            String summary = modifiedChanges.isEmpty() ? ""
                    : modifiedChanges.entrySet().stream()
                    .map(e -> e.getValue() + " " + e.getKey())
                    .collect(Collectors.joining(", ", " [", "]"));
            recordChanges.put(String.format("modified%s", summary), this.modifiedRecords);
        }

        if (this.deletedRecords > 0)
            recordChanges.put("deleted", this.deletedRecords);

        StringBuilder builder = new StringBuilder();
        Iterator<Entry<String, Integer>> it = recordChanges.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, Integer> entry = it.next();
            Entry<String, Integer> first = recordChanges.entrySet().iterator().next();

            if (entry.equals(first))
                builder.append(String.format("%d records %s", entry.getValue(), entry.getKey()));
            else
                builder.append(String.format("%d %s", entry.getValue(), entry.getKey()));

            if (it.hasNext())
                builder.append(", ");
        }

        return builder.toString();
    }


    @Override
    public int hashCode() {
        return Objects.hash(insertedRecords, insertedValues, insertedQuality, insertedAnnotations, modifiedRecords, modifiedValues, modifiedQuality, modifiedAnnotations, deletedRecords);
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof SeriesChange) {
            SeriesChange that = (SeriesChange) object;
            return Objects.equals(this.insertedRecords, that.insertedRecords)
                    && Objects.equals(this.insertedValues, that.insertedValues)
                    && Objects.equals(this.insertedQuality, that.insertedQuality)
                    && Objects.equals(this.insertedAnnotations, that.insertedAnnotations)
                    && Objects.equals(this.modifiedRecords, that.modifiedRecords)
                    && Objects.equals(this.modifiedValues, that.modifiedValues)
                    && Objects.equals(this.modifiedQuality, that.modifiedQuality)
                    && Objects.equals(this.modifiedAnnotations, that.modifiedAnnotations)
                    && Objects.equals(this.deletedRecords, that.deletedRecords);
        }
        return false;
    }


    public void addChange(SeriesChange other) {
        this.insertedRecords += other.insertedRecords;
        this.insertedValues += other.insertedValues;
        this.insertedQuality += other.insertedQuality;
        this.insertedAnnotations += other.insertedAnnotations;
        this.modifiedRecords += other.modifiedRecords;
        this.modifiedValues += other.modifiedValues;
        this.modifiedQuality += other.modifiedQuality;
        this.modifiedAnnotations += other.modifiedAnnotations;
        this.deletedRecords += other.deletedRecords;
    }


    public void addInsertedField(JtsField inserted) {
        this.insertedRecords += 1;
        this.insertedValues += inserted.hasValue() ? 1 : 0;
        this.insertedQuality += inserted.hasQuality() ? 1 : 0;
        this.insertedAnnotations += inserted.hasAnnotation() ? 1 : 0;
    }


    public void addModifiedField(JtsField existing, JtsField modified) {
        this.modifiedRecords += 1;

        if (modified.hasValue()) {
            if (modified.getValue() != null || existing.hasValue())
                this.modifiedValues += 1;
        }

        if (modified.hasQuality()) {
            if (modified.getQuality() != null || existing.hasQuality())
                this.modifiedQuality += 1;
        }

        if (modified.hasAnnotation()) {
            if (modified.getAnnotation() != null || existing.hasAnnotation())
                this.modifiedAnnotations += 1;
        }
    }


    public void addDeletedField() {
        this.deletedRecords += 1;
    }


    public void addInserted(Integer records, Integer values, Integer quality, Integer annotations) {
        this.insertedRecords += records;
        this.insertedValues += values;
        this.insertedQuality += quality;
        this.insertedAnnotations += annotations;
    }


    public void addModified(Integer records, Integer values, Integer quality, Integer annotations) {
        this.modifiedRecords += records;
        this.modifiedValues += values;
        this.modifiedQuality += quality;
        this.modifiedAnnotations += annotations;
    }


    public void addDeleted(Integer records) {
        this.deletedRecords += records;
    }

}
