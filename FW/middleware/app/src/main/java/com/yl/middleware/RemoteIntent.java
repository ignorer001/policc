package com.yl.middleware;


import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;

import java.io.Serializable;

public class RemoteIntent extends Intent implements Serializable, Parcelable {
    public static final Parcelable.Creator<RemoteIntent> CREATOR
            = new Parcelable.Creator<RemoteIntent>() {
        public RemoteIntent createFromParcel(Parcel in) {
            return new RemoteIntent(in);
        }

        public RemoteIntent[] newArray(int size) {
            return new RemoteIntent[size];
        }
    };
    public String mAction;
    public Uri mData;
    public String mType;
    public String mPackage;
    public ComponentName mComponent;
    public int mFlags;
    public ArraySet<String> mCategories;
    public Bundle mExtras;
    public Rect mSourceBounds;
    public Intent mSelector;
    public ClipData mClipData;
    public int mContentUserHint;
    public byte[] mEncyptedParasite;


    public RemoteIntent(Intent o) {
        this.mAction = o.getAction();
        this.mData = o.getData();
        this.mType = o.getType();
        this.mPackage = o.getPackage();
        this.mComponent = o.getComponent();
        this.mFlags = o.getFlags();
        this.mContentUserHint = o.describeContents();
        if (o.getCategories() != null) {
            ArraySet<String> categories = (ArraySet<String>) o.getCategories();
            this.mCategories = new ArraySet<String>(categories);
        }
        if (o.getExtras() != null) {
            this.mExtras = new Bundle(o.getExtras());
        }
        if (o.getSourceBounds() != null) {
            this.mSourceBounds = new Rect(o.getSourceBounds());
        }
        if (o.getSelector() != null) {
            this.mSelector = new Intent(o.getSelector());
        }
        if (o.getClipData() != null) {
            this.mClipData = new ClipData(o.getClipData());
        }
    }

    /**
     * @hide
     */
    protected RemoteIntent(Parcel in) {
        readFromParcel(in);
    }

    public Intent backtoIntent() {
        Intent i = new Intent();
        i.setAction(mAction);
        i.setData(mData);
        i.setType(mType);
        i.setPackage(mPackage);
        i.setComponent(mComponent);
        i.setFlags(mFlags);
        if (mCategories != null) {
            for (String category : mCategories) {
                i.addCategory(category);
            }
        }
        if (mExtras != null)
            i.putExtras(mExtras);
        if (mSourceBounds != null)
            i.setSourceBounds(mSourceBounds);
        if (mSelector != null)
            i.setSelector(mSelector);
        if (mClipData != null)
            i.setClipData(mClipData);

        if (mEncyptedParasite != null) {
            i.putExtra("PARASITE", mEncyptedParasite);
        }


        return i;
    }

    public RemoteIntent setAction(String action) {
        mAction = action != null ? action.intern() : null;
        return this;
    }


    public void readFromParcel(Parcel in) {
        setAction(in.readString());
        mData = Uri.CREATOR.createFromParcel(in);
        mType = in.readString();
        mFlags = in.readInt();
        mPackage = in.readString();
        mComponent = ComponentName.readFromParcel(in);

        if (in.readInt() != 0) {
            mSourceBounds = Rect.CREATOR.createFromParcel(in);
        }

        int N = in.readInt();
        if (N > 0) {
            mCategories = new ArraySet<String>();
            int i;
            for (i = 0; i < N; i++) {
                mCategories.add(in.readString().intern());
            }
        } else {
            mCategories = null;
        }
        mContentUserHint = in.readInt();
        mExtras = in.readBundle();

        int length = in.readInt();

        if (length != 0) {
            mEncyptedParasite = new byte[length];
            in.readByteArray(mEncyptedParasite);
        }

    }

    @Override
    public int describeContents() {
        return (mExtras != null) ? mExtras.describeContents() : 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mAction);
        Uri.writeToParcel(out, mData);
        out.writeString(mType);
        out.writeInt(mFlags);
        out.writeString(mPackage);
        ComponentName.writeToParcel(mComponent, out);

        if (mSourceBounds != null) {
            out.writeInt(1);
            mSourceBounds.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }

        if (mCategories != null) {
            final int N = mCategories.size();
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                out.writeString(mCategories.valueAt(i));
            }
        } else {
            out.writeInt(0);
        }

        out.writeInt(mContentUserHint);
        out.writeBundle(mExtras);

        if (mEncyptedParasite != null) {
            out.writeInt(mEncyptedParasite.length);
            out.writeByteArray(mEncyptedParasite);
        } else {
            out.writeInt(0);
        }

    }

    public void putParasiteExtra(byte[] encyptedParasite) {
        //FIXME: clone maybe a shallow copy, here maybe we need deep copy!
        //this.mEncyptedParasite = (encyptedParasite == null) ? null : encyptedParasite.clone();
        if (encyptedParasite != null) {
            this.mEncyptedParasite = new byte[encyptedParasite.length];
            System.arraycopy(encyptedParasite, 0, this.mEncyptedParasite, 0, encyptedParasite.length);
        }
    }

}