package com.github.manolo8.darkbot.extensions.util;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonAdapter(Version.VersionAdapter.class)
public class Version implements eu.darkbot.api.utils.Version {
    private static final Pattern VERSION = Pattern.compile("" +
            "([^0-9]*[0-9]+)" + // Major
            "(\\.([0-9]+))?" + // Minor
            "(\\.([0-9]+))?" + // Patch
            "(\\.([0-9]+))?" + // Revision
            "( ?beta ?([0-9]+)?)?" + // Beta
            "( ?alpha ?([0-9]+)?)?"); // Alpha

    private final String version;
    private final int major, minor, patch, revision, beta, alpha;

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public int getRevision() {
        return revision;
    }

    public boolean isBeta() {
        return beta != Integer.MAX_VALUE;
    }

    public int getBeta() {
        return beta;
    }

    public boolean isAlpha() {
        return alpha != Integer.MAX_VALUE;
    }

    public int getAlpha() {
        return alpha;
    }

    public Version(String version) {
        this.version = version;
        Matcher matcher = VERSION.matcher(version);
        if (!matcher.matches()) throw new IllegalArgumentException("Couldn't parse version " + version);
        major = Integer.parseInt(matcher.group(1));
        minor = getInt(matcher, 2);
        patch = getInt(matcher, 4);
        revision = getInt(matcher, 6);

        int tmpBeta = getInt(matcher, 8);
        int tmpAlpha = getInt(matcher, 10);
        beta = tmpBeta == -1 && tmpAlpha == -1 ? Integer.MAX_VALUE : tmpBeta;
        alpha = tmpAlpha == -1 ? Integer.MAX_VALUE : tmpAlpha;
    }

    public Version(int major, int minor, int patch) {
        this(major, minor, patch, -1, -1, -1);
    }

    public Version(int major, int minor, int patch, int revision, int beta, int alpha) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.revision = revision;

        this.beta = beta == -1 && alpha == -1 ? Integer.MAX_VALUE : beta;
        this.alpha = alpha == -1 ? Integer.MAX_VALUE : alpha;
        this.version = major + "." + minor + "." + patch + "." + revision + " beta " + beta + " alpha " + alpha;
    }

    private int getInt(Matcher m, int find) {
        if (m.group(find) == null) return -1;
        String num = m.group(find + 1);
        return num == null ? 0 : Integer.parseInt(m.group(find + 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version v = (Version) o;
        return major == v.major &&
                minor == v.minor &&
                patch == v.patch &&
                revision == v.revision &&
                beta == v.beta &&
                alpha == v.alpha;
    }

    // Kept for backwards compat of plugins built against darkbot
    // Plugins built against api will use super.compareTo already
    public int compareTo(Version o) {
        return eu.darkbot.api.utils.Version.super.compareTo(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, revision, beta, alpha);
    }

    @Override
    public String toString() {
        return version;
    }

    public static class VersionAdapter extends TypeAdapter<Version> {
        @Override
        public void write(JsonWriter writer, Version value) throws IOException {
            writer.value(value.toString());
        }

        @Override
        public Version read(JsonReader in) throws IOException {
            return new Version(in.nextString());
        }
    }
}
