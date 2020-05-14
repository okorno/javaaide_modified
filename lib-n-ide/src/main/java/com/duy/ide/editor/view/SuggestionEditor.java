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

package com.duy.ide.editor.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.duy.common.DLog;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.editor.R;
import com.duy.ide.editor.internal.suggestion.OnSuggestItemClickListener;
import com.duy.ide.editor.internal.suggestion.SuggestionAdapter;
import com.duy.ide.editor.theme.model.EditorTheme;
import com.jecelyin.common.utils.SysUtils;

import java.util.ArrayList;
import java.util.List;

public class SuggestionEditor extends EditActionSupportEditor implements AdapterView.OnItemClickListener {

    private static final String TAG = "SuggestionEditor";
    private final Rect mTmpRect = new Rect();

    private SuggestionAdapter mAdapter;

    /**
     * Small popup window to display list suggestion
     */
    private ListPopupWindow mPopup;
    /**
     * Only show popup when suggestion is enable
     */
    private boolean mSuggestionEnable = false;

    private final Runnable mPostChangePopupPosition = new Runnable() {
        @Override
        public void run() {
            onPopupChangePosition();
        }
    };


    public SuggestionEditor(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SuggestionEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);

    }

    public SuggestionEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mAdapter = new SuggestionAdapter(getContext(), new ArrayList<SuggestItem>());
//        mAdapter.setListener(this);

        mPopup = new ListPopupWindow(context, attrs, defStyleAttr, 0);
        mPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mPopup.setAdapter(mAdapter);
        mAdapter.setListener(this);

        if (getEditorTheme() != null) {
            setThemeForPopup(getEditorTheme());
        }

        setSuggestEnable(mPreferences.isUseAutoComplete());
    }

    private void setThemeForPopup(EditorTheme theme) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(theme.getDropdownBgColor());
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(SysUtils.dpToPixels(getContext(), 4));
        background.setStroke(SysUtils.dpToPixels(getContext(), 1), theme.getDropdownBorderColor());

        mPopup.setBackgroundDrawable(background);
        mAdapter.setTextColor(theme.getDropdownFgColor());
    }

    @Override
    public void setTheme(@NonNull EditorTheme editorTheme) {
        super.setTheme(editorTheme);
        if (mPopup != null) {
            setThemeForPopup(editorTheme);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);
        postUpdatePosition();
    }

    private void postUpdatePosition() {
        if (!mSuggestionEnable) {
            return;
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mPostChangePopupPosition);
            mHandler.postDelayed(mPostChangePopupPosition, 50);
        }
    }

    protected void onPopupChangePosition() {
        if (!mSuggestionEnable) {
            return;
        }
        try {
            Layout layout = getLayout();
            //not ready
            if (layout == null) {
                return;
            }
            int cursor = getSelectionStart();
            if (cursor < 0) {
                return;
            }
            int line = layout.getLineForOffset(cursor);
            int bottom = layout.getLineBottom(line);
            int top = layout.getLineTop(line);

            float cursorX = layout.getPrimaryHorizontal(cursor);

            //left of cursor
            int offsetHorizontal = (int) cursorX + getPaddingLeft() - getScrollX();
            setDropDownHorizontalOffset(offsetHorizontal);

            getGlobalVisibleRect(mTmpRect);
            int heightVisible = mTmpRect.height();

            int popupBottom = bottom - getScrollY() + getDropDownHeight();
            if (popupBottom < heightVisible) {
                popupBottom = bottom - getScrollY() + getDropDownHeight();
                setDropDownVerticalOffset(popupBottom);
            } else {
                int popupTop = top - getScrollY() - getDropDownHeight();
                setDropDownVerticalOffset(popupTop + getDropDownHeight());
            }
        } catch (Exception ignored) {
            if (DLog.DEBUG) DLog.e(TAG, "onPopupChangePosition: ", ignored);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onDropdownChangeSize();
    }

    /**
     * this method will be change size of popup window
     */
    protected void onDropdownChangeSize() {
        Rect rect = new Rect();
        getGlobalVisibleRect(rect);
        if (DLog.DEBUG) DLog.d(TAG, "rect = " + rect);
        // 1/2 width of screen
        setDropDownWidth((int) (rect.width() * 0.65f));
        // 0.5 height of screen
        setDropDownHeight((int) (rect.height() * 0.5f));
        onPopupChangePosition();
    }

    public void showDropDown() {
        if (!mSuggestionEnable) {
            return;
        }
        if (!isPopupShowing()) {
            if (hasFocus()) {
                try {
                    showDropDownImpl();
                } catch (Exception e) {
                    if (DLog.DEBUG) DLog.e(TAG, "showDropDown: ", e);
                }
            }
        }
    }

    /**
     * <p>Displays the drop down on screen.</p>
     */
    private void showDropDownImpl() {
        if (mPopup.getAnchorView() == null) {
            mPopup.setAnchorView(this);
        }
        if (!isPopupShowing()) {
            // Make sure the list does not obscure the IME when shown for the first time.
            mPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
        }
        mPopup.show();
        ListView list = mPopup.getListView();
        list.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    }


    @Override
    public void setSuggestEnable(boolean enable) {
        mSuggestionEnable = enable;
        if (mSuggestionEnable) {
            onDropdownChangeSize();
        } else {
            dismissDropDown();
        }
    }

    /**
     * invalidate data for auto suggest
     */
    @Override
    public void setSuggestData(@NonNull List<SuggestItem> data) {
        if (!mSuggestionEnable) {
            return;
        }
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();

        if (data.size() > 0) {
            showDropDown();
        } else {
            if (isPopupShowing()) {
                dismissDropDown();
            }
        }
    }

    @Override
    public void setOnSuggestItemClickListener(@Nullable OnSuggestItemClickListener listener) {
    }

    /**
     * <p>Gets the vertical offset used for the auto-complete drop-down list.</p>
     *
     * @return the vertical offset
     * @attr ref android.R.styleable#ListPopupWindow_dropDownVerticalOffset
     */
    public int getDropDownVerticalOffset() {
        return mPopup.getVerticalOffset();
    }

    /**
     * <p>Sets the vertical offset used for the auto-complete drop-down list.</p>
     *
     * @param offset the vertical offset
     * @attr ref android.R.styleable#ListPopupWindow_dropDownVerticalOffset
     */
    public void setDropDownVerticalOffset(int offset) {
        if (offset != mPopup.getVerticalOffset()) {
            mPopup.setVerticalOffset(offset);
        }
    }

    /**
     * <p>Gets the horizontal offset used for the auto-complete drop-down list.</p>
     *
     * @return the horizontal offset
     * @attr ref android.R.styleable#ListPopupWindow_dropDownHorizontalOffset
     */
    public int getDropDownHorizontalOffset() {
        return mPopup.getHorizontalOffset();
    }

    /**
     * <p>Sets the horizontal offset used for the auto-complete drop-down list.</p>
     *
     * @param offset the horizontal offset
     * @attr ref android.R.styleable#ListPopupWindow_dropDownHorizontalOffset
     */
    public void setDropDownHorizontalOffset(int offset) {
        if (offset != mPopup.getHorizontalOffset()) {
            mPopup.setHorizontalOffset(offset);
        }
    }

    /**
     * <p>Returns the current height for the auto-complete drop down list. This can
     * be a fixed height, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill
     * the screen, or {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the height
     * of the drop down's content.</p>
     *
     * @return the height for the drop down list
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownHeight
     */
    public int getDropDownHeight() {
        return mPopup.getHeight();
    }

    /**
     * <p>Sets the current height for the auto-complete drop down list. This can
     * be a fixed height, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill
     * the screen, or {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the height
     * of the drop down's content.</p>
     *
     * @param height the height to use
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownHeight
     */
    public void setDropDownHeight(int height) {
        mPopup.setHeight(height);
    }

    /**
     * <p>Sets the current width for the auto-complete drop down list. This can
     * be a fixed width, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill the screen, or
     * {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the width of its anchor view.</p>
     *
     * @param width the width to use
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownWidth
     */
    public void setDropDownWidth(int width) {
        mPopup.setWidth(width);
    }

    /**
     * <p>Indicates whether the popup menu is showing.</p>
     *
     * @return true if the popup menu is showing, false otherwise
     */
    public boolean isPopupShowing() {
        return mPopup.isShowing();
    }

    /**
     * <p>Closes the drop down if present on screen.</p>
     */
    public void dismissDropDown() {
        try {
            if (mPopup.isShowing()) {
                mPopup.dismiss();
            }
        } catch (Exception e) {
        }
    }

    public void setAdapter(SuggestionAdapter adapter) {
        mAdapter = adapter;
        mPopup.setAdapter(mAdapter);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus && !mPopup.isDropDownAlwaysVisible()) {
            dismissDropDown();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onDisplayHint(int hint) {
        super.onDisplayHint(hint);
        if (hint == INVISIBLE) {
            if (!mPopup.isDropDownAlwaysVisible()) {
                dismissDropDown();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isTemporarilyDetached()) {
                // If we are temporarily in the detach state, then do nothing.
                return;
            }
        }
        if (!focused && !mPopup.isDropDownAlwaysVisible()) {
            dismissDropDown();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        dismissDropDown();
        mHandler.removeCallbacks(mPostChangePopupPosition);
        super.onDetachedFromWindow();
    }

    @Override
    protected boolean setFrame(final int l, int t, final int r, int b) {
        boolean result = super.setFrame(l, t, r, b);

        if (isPopupShowing()) {
            showDropDown();
        }

        return result;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isPopupShowing()) {
            //editor support press tab to insert tab character
            if (keyCode != KeyEvent.KEYCODE_TAB) {
                mPopup.onKeyUp(keyCode, event);
                switch (keyCode) {
                    // if the list accepts the key events and the key event
                    // was a click, the text view gets the selected item
                    // from the drop down as its content
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (event.hasNoModifiers()) {
                            performCompletion();
                        }
                        return true;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private int getSelectedItemPosition() {
        return mPopup.getListView().getSelectedItemPosition();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isPopupShowing()) {
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (getSelectedItemPosition() >= 0) {
                    //#onKeyUp will be perform completion
                    return true;
                }
            }

            //editor support press tab to insert tab character
            if (keyCode != KeyEvent.KEYCODE_TAB) {
                if (mPopup.onKeyDown(keyCode, event)) {
                    return true;
                }
            }
            boolean handled = super.onKeyDown(keyCode, event);
            if (handled) {
                clearListSelection();
            }
            return handled;
        } else {
            switch (keyCode) {
                //always return true because we need focus to editor
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_UP_LEFT:
                case KeyEvent.KEYCODE_DPAD_UP_RIGHT:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_DOWN_LEFT:
                case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT:
                    super.onKeyDown(keyCode, event);
                    return true;
                default:
                    return super.onKeyDown(keyCode, event);
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void performCompletion() {
        performCompletion(-1);
    }

    @SuppressLint("RestrictedApi")
    private void performCompletion(int position) {
        if (isPopupShowing() && mAdapter != null) {
            if (position < 0) {
                position = mPopup.getSelectedItemPosition();
            }
            if (position < 0) {
                return;
            }
            SuggestItem item = mAdapter.getItem(position);
            if (item != null) {
                //handle by this item
                item.onSelectThis(SuggestionEditor.this);
            }
        }

        if (!mPopup.isDropDownAlwaysVisible()) {
            dismissDropDown();
            requestFocus();
        }
    }

    /**
     * <p>Clear the list selection.  This may only be temporary, as user input will often bring
     * it back.
     */
    public void clearListSelection() {
        mPopup.clearListSelection();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if (key.equals(getContext().getString(R.string.pref_auto_complete))) {
            mSuggestionEnable = mPreferences.isUseAutoComplete();
            setSuggestEnable(mSuggestionEnable);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        performCompletion(position);
    }
}
