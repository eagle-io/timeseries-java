package io.eagle.util.jts;

public class MetricDef {
        private String id;
        private String unit;

        public MetricDef() {}

        public MetricDef(String id, String unit) {
                this.id = id;
                this.unit = unit;
        }

        public String getId() {
                return id;
        }

        public String getUnit() {
                return unit;
        }
}
