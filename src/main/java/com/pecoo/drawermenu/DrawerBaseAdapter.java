package com.pecoo.drawermenu;

import android.database.Observable;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jzm on 2018/3/22.
 * Description:
 */
public abstract class DrawerBaseAdapter<DH extends DrawerHolder> {

    private AdapterDataObservable mObservable = new AdapterDataObservable();

    protected abstract int getTabCount();

    protected abstract DH createViewHolder(int viewType);

    protected abstract void onBindViewHolder(DH viewHolder, int position);

    protected abstract View getPopupView(int position, ViewGroup parent);

    protected abstract View getContentView();

    protected int getViewType(int position){
        return  position;
    }

    protected abstract void setSelected(boolean isSelected, int position);

    public final boolean hasObservers() {
        return mObservable.hasObservers();
    }

    public void registerAdapterDataObserver(DrawerMenuView.AdapterDataObserver observer) {
        mObservable.registerObserver(observer);
    }

    public void unRegisterAdapterDataObserver(DrawerMenuView.AdapterDataObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    public final void notifyChanged() {
        mObservable.notifyChanged();
    }

    public final void notifyChangedByPosition(int position) {
        mObservable.notifyChangedByPosition(position);
    }


    static class AdapterDataObservable extends Observable<DrawerMenuView.AdapterDataObserver> {
        public boolean hasObservers() {
            return !mObservers.isEmpty();
        }

        public void notifyChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }

        public void notifyChangedByPosition(int position) {
            mObservers.get(position).onChanged(position);
        }
    }

}
