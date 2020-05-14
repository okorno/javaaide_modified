/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.diagnostic.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Message implements Serializable {

    @NonNull
    private final Kind mKind;

    @NonNull
    private final String mText;

    @NonNull
    private final ArrayList<SourceFilePosition> mSourceFilePositions = new ArrayList<>();

    @NonNull
    private final String mRawMessage;

    /**
     * Create a new message, which has a {@link Kind}, a String which will be shown to the user and
     * at least one {@link SourceFilePosition}.
     *
     * @param kind                the message type.
     * @param text                the text of the message.
     * @param sourceFilePosition  the first source file position the message .
     * @param sourceFilePositions any additional source file positions, may be empty.
     */
    public Message(@NonNull Kind kind,
                   @NonNull String text,
                   @NonNull SourceFilePosition sourceFilePosition,
                   @NonNull SourceFilePosition... sourceFilePositions) {
        mKind = kind;
        mText = text;
        mRawMessage = text;
        mSourceFilePositions.add(sourceFilePosition);
        mSourceFilePositions.addAll(Arrays.asList(sourceFilePositions));
    }

    /**
     * Create a new message, which has a {@link Kind}, a String which will be shown to the user and
     * at least one {@link SourceFilePosition}.
     * <p>
     * It also has a rawMessage, to store the original string for cases when the message is
     * constructed by parsing the output from another tool.
     *
     * @param kind                the message kind.
     * @param text                a human-readable string explaining the issue.
     * @param rawMessage          the original text of the message, usually from an external tool.
     * @param sourceFilePosition  the first source file position.
     * @param sourceFilePositions any additional source file positions, may be empty.
     */
    public Message(@NonNull Kind kind,
                   @NonNull String text,
                   @NonNull String rawMessage,
                   @NonNull SourceFilePosition sourceFilePosition,
                   @NonNull SourceFilePosition... sourceFilePositions) {
        mKind = kind;
        mText = text;
        mRawMessage = rawMessage;
        mSourceFilePositions.add(sourceFilePosition);
        mSourceFilePositions.addAll(Arrays.asList(sourceFilePositions));
    }

    public Message(@NonNull Kind kind,
                   @NonNull String text,
                   @NonNull String rawMessage,
                   @NonNull ImmutableList<SourceFilePosition> positions) {
        mKind = kind;
        mText = text;
        mRawMessage = rawMessage;

        if (positions.isEmpty()) {
            mSourceFilePositions.add(SourceFilePosition.UNKNOWN);
        } else {
            mSourceFilePositions.addAll(positions);
        }
    }

    @NonNull
    public Kind getKind() {
        return mKind;
    }

    @NonNull
    public String getText() {
        return mText;
    }

    /**
     * Returns a list of source positions. Will always contain at least one item.
     */
    @NonNull
    public List<SourceFilePosition> getSourceFilePositions() {
        return mSourceFilePositions;
    }

    @NonNull
    public String getRawMessage() {
        return mRawMessage;
    }

    @Nullable
    public String getSourcePath() {
        File file = mSourceFilePositions.get(0).getFile().getSourceFile();
        if (file == null) {
            return null;
        }
        return file.getAbsolutePath();
    }

    /**
     * Returns a legacy 1-based line number.
     */
    @Deprecated
    public int getLineNumber() {
        return mSourceFilePositions.get(0).getPosition().getStartLine() + 1;
    }

    /**
     * @return a legacy 1-based column number.
     */
    @Deprecated
    public int getColumn() {
        return mSourceFilePositions.get(0).getPosition().getStartColumn() + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        Message that = (Message) o;
        return Objects.equal(mKind, that.mKind) &&
                Objects.equal(mText, that.mText) &&
                Objects.equal(mSourceFilePositions, that.mSourceFilePositions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mKind, mText, mSourceFilePositions);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("kind", mKind).add("text", mText).add("sources",
                mSourceFilePositions).toString();
    }

    public enum Kind {
        ERROR, WARNING, INFO, STATISTICS, UNKNOWN, SIMPLE;

        public static Kind findIgnoringCase(String s, Kind defaultKind) {
            for (Kind kind : values()) {
                if (kind.toString().equalsIgnoreCase(s)) {
                    return kind;
                }
            }
            return defaultKind;
        }

        @Nullable
        public static Kind findIgnoringCase(String s) {
            return findIgnoringCase(s, null);
        }
    }
}
