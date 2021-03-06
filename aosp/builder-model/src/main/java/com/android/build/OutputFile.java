package com.android.build;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import java.io.File;
import java.util.Collection;
/**
 * An output with an associated set of filters.
 */
public interface OutputFile {
    /**
     * An object representing the lack of filter.
     */
    public static final String NO_FILTER = null;
    /**
     * Type of package file, either the main APK or a pure split APK file containing resources for
     * a particular split dimension.
     */
    public enum OutputType {
        MAIN, FULL_SPLIT, SPLIT
    }
    /**
     * String representation of the OutputType enum which can be used for remote-able interfaces.
     */
    public static final String MAIN = OutputType.MAIN.name();
    public static final String FULL_SPLIT = OutputType.FULL_SPLIT.name();
    public static final String SPLIT = OutputType.SPLIT.name();
    /**
     * Split dimension type
     */
    public enum FilterType {
        DENSITY, ABI, LANGUAGE
    }
    /**
     * String representations of the FilterType enum which can be used for remote-able interfaces.
     */
    public static final String DENSITY = FilterType.DENSITY.name();
    public static final String ABI = FilterType.ABI.name();
    public static final String LANGUAGE = FilterType.LANGUAGE.name();
    /**
     * Returns the output type of the referenced APK.
     */
    @NonNull
    String getOutputType();
    /**
     * Returns the split dimensions the referenced APK was created with. Each collection's value
     * is the string representation of an element of the {@see FilterType} enum.
     */
    @NonNull
    public Collection<String> getFilterTypes();
    /**
     * Returns all the split information used to create the APK.
     */
    @NonNull
    public Collection<FilterData> getFilters();
    /**
     * Returns the output file for this artifact's output.
     * Depending on whether the project is an app or a library project, this could be an apk or
     * an aar file. If this {@link com.android.build.OutputFile} has filters, this is a split
     * APK.
     *
     * For test artifact for a library project, this would also be an apk.
     *
     * @return the output file.
     */
    @NonNull
    File getOutputFile();
}