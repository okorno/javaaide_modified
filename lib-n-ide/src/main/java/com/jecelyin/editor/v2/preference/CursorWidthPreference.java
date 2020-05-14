/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.editor.v2.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.duy.ide.editor.editor.R;
import com.jecelyin.common.utils.SysUtils;
import com.jecelyin.editor.v2.adapter.RangeAdapter;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class CursorWidthPreference extends JecListPreference {
    public CursorWidthPreference(Context context) {
        super(context);
        init();
    }

    public CursorWidthPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void init() {
        ItemAdapter adapter = new ItemAdapter(1, 6, "%d sp");
        setEntries(adapter.getItems());
        setEntryValues(adapter.getValues());
        setAdapter(adapter);
    }

    static class ItemAdapter extends RangeAdapter {

        public ItemAdapter(int min, int max, String format) {
            super(min, max, format);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            int value = getValue(position);

            View cursorView = (View) view.getTag(R.id.cursorView);
            if (cursorView == null) {
                cursorView = view.findViewById(R.id.cursorView);
                view.setTag(R.id.cursorView, cursorView);
            }

            ViewGroup.LayoutParams lp = cursorView.getLayoutParams();
            if (lp == null)
                lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.width = SysUtils.dpToPixels(parent.getContext(), value);
            cursorView.setLayoutParams(lp);

            return view;
        }

        @Override
        protected int getLayoutResId() {
            return R.layout.pref_cursor_width_layout;
        }
    }
}
