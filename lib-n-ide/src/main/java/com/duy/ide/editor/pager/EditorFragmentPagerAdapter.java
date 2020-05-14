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

package com.duy.ide.editor.pager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.v4.ArrayPagerAdapter;
import com.duy.ide.editor.EditorDelegate;
import com.duy.ide.editor.EditorFragment;
import com.duy.ide.editor.IEditorDelegate;
import com.jecelyin.editor.v2.adapter.TabAdapter;
import com.jecelyin.editor.v2.common.TabCloseListener;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 25-Apr-18.
 */

public class EditorFragmentPagerAdapter extends ArrayPagerAdapter<EditorFragment> implements IEditorPagerAdapter {
    private static final String TAG = "EditorFragmentPagerAdap";

    public EditorFragmentPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager(), new ArrayList<PageDescriptor>());
    }

    @Override
    public EditorFragment getCurrentFragment() {
        return super.getCurrentFragment();
    }

    @Override
    public void removeAll(TabCloseListener tabCloseListener) {
        while (getCount() > 0) {
            removeEditor(0, tabCloseListener);
        }
    }

    @Override
    public void newEditor(@NonNull File file, int offset, String encoding) {
        add(new EditorPageDescriptor(file, offset, encoding));
    }

    @Nullable
    @Override
    public EditorDelegate getCurrentEditorDelegate() {
        if (getCount() == 0) {
            return null;
        }
        EditorFragment fragment = (EditorFragment) getCurrentFragment();
        if (fragment != null) {
            return fragment.getEditorDelegate();
        }
        return null;
    }

    @Override
    public TabAdapter.TabInfo[] getTabInfoList() {
        int size = getCount();
        TabAdapter.TabInfo[] arr = new TabAdapter.TabInfo[size];
        for (int i = 0; i < size; i++) {
            EditorDelegate editorDelegate = getEditorDelegateAt(i);
            if (editorDelegate != null) {
                boolean changed = editorDelegate.isChanged();
                arr[i] = new TabAdapter.TabInfo(editorDelegate.getTitle(), editorDelegate.getPath(), changed);
            } else {
                EditorPageDescriptor pageDescriptor = (EditorPageDescriptor) getPageDescriptor(i);
                arr[i] = new TabAdapter.TabInfo(pageDescriptor.getTitle(), pageDescriptor.getPath(), false);
            }
        }

        return arr;
    }

    @Override
    public void removeEditor(final int position, final TabCloseListener listener) {
        EditorDelegate delegate = getEditorDelegateAt(position);
        if (delegate == null) {
            //not init
            return;
        }

        final String encoding = delegate.getEncoding();
        final int offset = delegate.getCursorOffset();
        final String path = delegate.getPath();
        //no need save file, all file will be auto save when EditorFragment destroy
        // if (delegate.isChanged()) {
        //     delegate.save(true);
        // }
        remove(position);
        if (listener != null) {
            listener.onClose(path, encoding, offset);
        }
    }

    public ArrayList<IEditorDelegate> getAllEditor() {
        ArrayList<IEditorDelegate> delegates = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            delegates.add(getEditorDelegateAt(i));
        }
        return delegates;
    }

    @Override
    protected EditorFragment createFragment(PageDescriptor desc) {
        return EditorFragment.newInstance((EditorPageDescriptor) desc);
    }

    @Override
    public EditorPageDescriptor getPageDescriptor(int position) {
        return (EditorPageDescriptor) super.getPageDescriptor(position);
    }

    @Nullable
    public EditorDelegate getEditorDelegateAt(int index) {
        EditorFragment fragment = getExistingFragment(index);
        if (fragment != null) {
            return fragment.getEditorDelegate();
        }
        return null;
    }

}
