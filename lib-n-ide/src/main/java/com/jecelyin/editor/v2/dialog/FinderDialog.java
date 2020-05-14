/*
 * Copyright 2018 Mr Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.editor.v2.dialog;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.duy.ide.database.ITabDatabase;
import com.duy.ide.database.SQLHelper;
import com.duy.ide.editor.editor.R;
import com.jecelyin.common.task.TaskListener;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.Preferences;
import com.duy.ide.editor.EditorDelegate;
import com.jecelyin.editor.v2.utils.ExtGrep;
import com.jecelyin.editor.v2.utils.GrepBuilder;
import com.jecelyin.editor.v2.utils.MatcherResult;


/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class FinderDialog extends AbstractDialog  {
    private static final int ID_FIND_PREV = 1;
    private static final int ID_FIND_NEXT = 2;
    private static final int ID_REPLACE = 3;
    private static final int ID_REPLACE_ALL = 4;
    private static final int ID_FIND_TEXT = 5;
    /**
     * 0 = find
     * 1 = replace
     * 2 = find in files
     */
    int mode = 0;

    EditorDelegate fragment;

    CharSequence findText;

    public FinderDialog(Context context) {
        super(context);
    }

    public static void showFindDialog(EditorDelegate fragment) {
        FinderDialog dialog = new FinderDialog(fragment.getContext());
        dialog.mode = 0;
        dialog.fragment = fragment;
        dialog.findText = fragment.getSelectedText();
        dialog.show();
    }

    public static void showReplaceDialog(EditorDelegate fragment) {
        FinderDialog dialog = new FinderDialog(fragment.getContext());
        dialog.mode = 1;
        dialog.fragment = fragment;
        dialog.findText = fragment.getSelectedText();
        dialog.show();
    }

    @Override
    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_find_replace_default, null);

        final ViewHolder holder = new ViewHolder(view);
//        holder.mFindEditText.setDrawableClickListener(this);
//        holder.mReplaceEditText.setDrawableClickListener(this);
        if (findText != null)
            holder.mFindEditText.setText(findText.toString());
        if (Preferences.getInstance(context).isReadOnly()) {
            holder.mReplaceCheckBox.setVisibility(View.GONE);
            holder.mReplaceEditText.setVisibility(View.GONE);
        } else {
            holder.mReplaceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    holder.mReplaceEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });
            holder.mReplaceCheckBox.setChecked(mode == 1);
            holder.mReplaceEditText.setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        }


        holder.mRegexCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UIUtils.toast(context, R.string.use_regex_to_find_tip);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.find_replace);
        builder.setView(view);
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(R.string.find, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onFindButtonClick(holder)) {
                    dialog.dismiss();
                }
            }
        });
//        builder.setNeutralButton(R.string.history, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
////                new FindHistoryDialog(context, (EditText) editText, editText.getId() != R.id.find_edit_text).show();
//            }
//        });
        AlertDialog dialog = builder.create();
        dialog.show();
        handleDialog(dialog);
    }

    private boolean onFindButtonClick(ViewHolder holder) {
        String findText = holder.mFindEditText.getText().toString();
        if (TextUtils.isEmpty(findText)) {
            holder.mFindEditText.setError(context.getString(R.string.cannot_be_empty));
            return false;
        }

        String replaceText = holder.mReplaceCheckBox.isChecked() ? holder.mReplaceEditText.getText().toString() : null;

        GrepBuilder builder = GrepBuilder.start();
        if (!holder.mCaseSensitiveCheckBox.isChecked()) {
            builder.ignoreCase();
        }
        if (holder.mWholeWordsOnlyCheckBox.isChecked()) {
            builder.wordRegex();
        }
        builder.setRegex(findText, holder.mRegexCheckBox.isChecked());

        ExtGrep grep = builder.build();

        ITabDatabase database = SQLHelper.getInstance(context);
        database.addFindKeyword(findText, false);
        database.addFindKeyword(replaceText, true);

        findNext(grep, replaceText);
        return true;
    }

    private void findNext(final ExtGrep grep, final String replaceText) {
        grep.grepText(ExtGrep.GrepDirect.NEXT,
                fragment.getEditableText(),
                fragment.getCursorOffset(),
                new TaskListener<MatcherResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onSuccess(MatcherResult match) {
                        if (match == null) {
                            UIUtils.toast(context, R.string.find_not_found);
                            return;
                        }
                        fragment.addHighlight(match.start(), match.end());
                        getMainActivity().startSupportActionMode(new FindTextActionModeCallback(replaceText, fragment, grep, match));
                    }

                    @Override
                    public void onError(Exception e) {
                        DLog.e(e);
                        UIUtils.toast(context, e.getMessage());
                    }
                }
        );
    }

    private static class FindTextActionModeCallback implements ActionMode.Callback {
        EditorDelegate fragment;
        ExtGrep grep;
        private String replaceText;
        private MatcherResult lastResults;

        public FindTextActionModeCallback(String replaceText, EditorDelegate fragment, ExtGrep grep, MatcherResult match) {
            this.replaceText = replaceText;
            this.fragment = fragment;
            this.grep = grep;
            this.lastResults = match;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle(null);
            actionMode.setSubtitle(null);

            View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.search_replace_action_mode_layout, null);
            int w = fragment.getContext().getResources().getDimensionPixelSize(R.dimen.cab_find_text_width);
            view.setLayoutParams(new ViewGroup.LayoutParams(w, ViewGroup.LayoutParams.MATCH_PARENT));

            TextView searchTextView = view.findViewById(R.id.searchTextView);
            searchTextView.setText(grep.getRegex());

            TextView replaceTextView = view.findViewById(R.id.replaceTextView);
            if (replaceText == null) {
                replaceTextView.setVisibility(View.GONE);
            } else {
                replaceTextView.setText(replaceText);
            }

            menu.add(0, ID_FIND_TEXT, 0, R.string.keyword)
                    .setActionView(view)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menu.add(0, ID_FIND_PREV, 0, R.string.previous_occurrence)
                    .setIcon(R.drawable.ic_keyboard_arrow_up_white)
                    .setAlphabeticShortcut('p')
                    .setShowAsAction(
                            MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            menu.add(0, ID_FIND_NEXT, 0, R.string.next_occurrence)
                    .setIcon(R.drawable.ic_keyboard_arrow_down_white)
                    .setAlphabeticShortcut('n')
                    .setShowAsAction(
                            MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            if (replaceText != null) {
                menu.add(0, ID_REPLACE, 0, R.string.replace)
                        .setIcon(R.drawable.ic_find_replace_white)
                        .setShowAsAction(
                                MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                menu.add(0, ID_REPLACE_ALL, 0, R.string.replace_all)
                        .setIcon(R.drawable.replace_all)
                        .setShowAsAction(
                                MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (TextUtils.isEmpty(grep.getRegex())) {
                UIUtils.toast(fragment.getContext(), R.string.find_keyword_is_empty);
                return false;
            }
            int id = menuItem.getItemId();
            switch (id) {
                case ID_FIND_PREV:
                case ID_FIND_NEXT:
                    doFind(id);
                    break;
                case ID_REPLACE:
                    if (lastResults != null) {
                        fragment.getEditableText().replace(lastResults.start(), lastResults.end(), ExtGrep.parseReplacement(lastResults, replaceText));
                        lastResults = null;
                    }
                    break;
                case ID_REPLACE_ALL:
                    grep.replaceAll(fragment.getEditText(), replaceText);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void doFind(int id) {
            id = id == ID_FIND_PREV ? ID_FIND_PREV : ID_FIND_NEXT;
            grep.grepText(id == ID_FIND_PREV ? ExtGrep.GrepDirect.PREV : ExtGrep.GrepDirect.NEXT,
                    fragment.getEditableText(),
                    fragment.getCursorOffset(),
                    new TaskListener<MatcherResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onSuccess(MatcherResult match) {
                            if (match == null) {
                                UIUtils.toast(fragment.getContext(), R.string.find_not_found);
                                return;
                            }
                            fragment.addHighlight(match.start(), match.end());
                            lastResults = match;
                        }

                        @Override
                        public void onError(Exception e) {
                            DLog.e(e);
                            UIUtils.toast(fragment.getContext(), e.getMessage());
                        }
                    });
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'search_replace.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Inmite Developers (http://inmite.github.io)
     */
    static class ViewHolder {
        EditText mFindEditText;
        EditText mReplaceEditText;
        CheckBox mReplaceCheckBox;
        CheckBox mCaseSensitiveCheckBox;
        CheckBox mWholeWordsOnlyCheckBox;
        CheckBox mRegexCheckBox;

        ViewHolder(View view) {
            mFindEditText = view.findViewById(R.id.find_edit_text);
            mReplaceEditText = view.findViewById(R.id.replace_edit_text);
            mReplaceCheckBox = view.findViewById(R.id.replace_check_box);
            mCaseSensitiveCheckBox = view.findViewById(R.id.case_sensitive_check_box);
            mWholeWordsOnlyCheckBox = view.findViewById(R.id.whole_words_only_check_box);
            mRegexCheckBox = view.findViewById(R.id.regex_check_box);
        }
    }
}
