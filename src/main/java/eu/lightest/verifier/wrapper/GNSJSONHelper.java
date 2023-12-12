package eu.lightest.verifier.wrapper;

import java.util.List;

public class GNSJSONHelper {
    private String record_name;
    private List<GNSRecord> data;

    public String getRecord_name() {
        return record_name;
    }

    public void setRecord_name(String record_name) {
        this.record_name = record_name;
    }

    public List<GNSRecord> getData() {
        return data;
    }

    public void setData(List<GNSRecord> data) {
        this.data = data;
    }

    public class GNSRecord {
        private String value;
        private String record_type;
        private int absolute_expiration;
        private boolean is_private;
        private boolean is_relative_expiration;
        private boolean is_supplemental;

        public int getAbsolute_expiration() {
            return absolute_expiration;
        }

        public void setAbsolute_expiration(int absolute_expiration) {
            this.absolute_expiration = absolute_expiration;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRecord_type() {
            return record_type;
        }

        public void setRecord_type(String record_type) {
            this.record_type = record_type;
        }

        public boolean isIs_private() {
            return is_private;
        }

        public void setIs_private(boolean is_private) {
            this.is_private = is_private;
        }

        public boolean isIs_relative_expiration() {
            return is_relative_expiration;
        }

        public void setIs_relative_expiration(boolean is_relative_expiration) {
            this.is_relative_expiration = is_relative_expiration;
        }

        public boolean isIs_supplemental() {
            return is_supplemental;
        }

        public void setIs_supplemental(boolean is_supplemental) {
            this.is_supplemental = is_supplemental;
        }
    }
}
