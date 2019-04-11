package com.project.gva.model;

public class Types {
    public enum DeviceStatus {
        ONLINE,
        OFFLINE;

        public static DeviceStatus fromName(String name) {
            for (DeviceStatus status : DeviceStatus.values()) {
                if (status.name().equals(name))
                    return status;
            }
            return null;
        }
    }

    public enum File {

        UPDATE("/update");
        private String bucket;

        public String bucket() {
            return bucket;
        }

        File(String bucket) {
            this.bucket = bucket;
        }
    }

    public enum Device {
        LIGHT,
        FAN;

        public static Device of(String s) {
            if (s == null)
                return null;

            for (Device device : Device.values()) {
                if (s.toUpperCase().contains(device.name()))
                    return device;
            }
            return null;
        }
    }

    public enum Tag {
        COMENTARIO,
        TYPE,
        VERSION;

        public static Tag of(String s) {
            if (s == null)
                return null;

            for (Tag tag : Tag.values()) {
                if (s.toUpperCase().contains(tag.name()))
                    return tag;
            }
            return null;
        }
    }

    public enum MessageTopic {
        PROCESO_ACTUALIZACION,
        PROCESO_VALIDACION,
        PROCESO_LOGIN,
        PROCESO_INSTALACION;
    }

    public enum MessageStatus {
        SUCCESS("green"),
        INFO("yellow"),
        WARNING("orange"),
        ERROR("red");

        private String style;

        public String style() {
            return style;
        }

        MessageStatus(String style) {
            this.style = style;
        }
    }
}
