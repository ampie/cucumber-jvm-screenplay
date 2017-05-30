package com.sbg.bdd.screenplay.core.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class Paths {
    public static File get(String name, String... segments) {
        String suffix = StringUtils.join(segments, File.separatorChar);
        return new File(name, suffix);
    }

    public static File get(File dir, String... segments) {
        String suffix = StringUtils.join(segments, File.separatorChar);
        return new File(dir, suffix);
    }
}
