package codexus.heypixel.plugin.messages;

public enum AntiCheatReportType { Info, BlackClass, BlackModule, ReflectCheck; public int value(){return ordinal();} public static AntiCheatReportType fromValue(int value){if(value<0||value>=values().length)throw new IllegalArgumentException("Unknown report type: "+value);return values()[value];} }
