package com.logginghub.utils;

import com.logginghub.utils.StringUtils.StringUtilsBuilder;

public class VersionNumber implements Comparable<VersionNumber> {

    private int major;
    private int minor = -1;
    private int build = -1;
    private String comment = null;
    private int parts;
    private String raw;

    public VersionNumber(int major, int minor, int build, String comment) {
        super();
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.comment = comment;
        
        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.add(major).add(".");
        builder.add(minor).add(".");
        builder.add(build);
        builder.addIfNotNull(comment, comment);
        this.raw = builder.toString();
    }

    public VersionNumber() {}

    public static VersionNumber parse(String versionNumber) {
        StringUtilsTokeniser st = new StringUtilsTokeniser(versionNumber);

        VersionNumber number = new VersionNumber();
        number.raw = versionNumber;
        try {
            number.major = st.nextInteger();

            if (st.hasMore()) {
                st.skip();
                number.minor = st.nextInteger();
            }

            if (st.hasMore()) {
                if (st.peekChar() == '.') {
                    st.skip();
                    if (!Character.isDigit(st.peekChar())) {
                        number.comment = st.restOfString();
                    }
                    else {
                        number.build = st.nextInteger();
                    }
                }
                else {
                    if (!Character.isDigit(st.peekChar())) {
                        number.comment = st.restOfString();
                    }
                }
            }

            if (st.hasMore()) {
                number.comment = st.restOfString();
            }
        }
        catch (RuntimeException e) {
//            parse(versionNumber);
//            throw e;
        }

        return number;
    }

    public static VersionNumber parse2(String versionNumber) {
        String[] split = versionNumber.split("\\.|-");

        VersionNumber number = new VersionNumber();
        number.major = Integer.parseInt(split[0]);
        number.parts = 1;

        if (split.length > 1) {
            try {
                number.minor = Integer.parseInt(split[1]);
            }
            catch (NumberFormatException e) {
                throw e;
            }
            number.parts = 2;
        }

        if (split.length > 2) {
            String thirdString = split[2];
            number.parts = 3;

            if (StringUtils.isStringNumeric(thirdString)) {
                number.build = Integer.parseInt(thirdString);
            }
            else {
                number.comment = thirdString;
            }
        }

        if (split.length > 3) {
            number.parts = 4;
            number.comment = split[3];
        }

        return number;
    }

    @Override public String toString() {
        //return StringUtils.build(major).addIfNotNegative(minor, ".", minor).addIfNotNegative(build, ".", build).addIfNotNull(comment, "", comment).toString();
        return raw;
    }

    public boolean hasComment() {
        return comment != null;
    }

    public String getComment() {
        return comment;
    }

    public int getBuild() {
        return build;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int compareTo(VersionNumber o) {
        return CompareUtils.add(this.major, o.major).add(this.minor, o.minor).add(this.build, o.build).add(this.comment, o.comment).compare();
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + build;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + major;
        result = prime * result + minor;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VersionNumber other = (VersionNumber) obj;
        if (build != other.build) return false;
        if (comment == null) {
            if (other.comment != null) return false;
        }
        else if (!comment.equals(other.comment)) return false;
        if (major != other.major) return false;
        if (minor != other.minor) return false;
        return true;
    }

    public VersionNumber getNextMajorVersion() {
        return new VersionNumber(major + 1, 0, 0, comment);
    }

    public VersionNumber getNextMinorVersion() {
        return new VersionNumber(major, minor + 1, 0, comment);
    }

    public VersionNumber getNextBuildVersion() {
        return new VersionNumber(major, minor, build + 1, comment);
    }

}
