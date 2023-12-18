package com.example.autoclicker.shared;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Each play is a coordinate representing where to tap the screen. A delay is used
 * so taps can be chained together and spaced out.
 *
 * */
public class Play implements Parcelable {
    private final float x;
    private final float y;
    private final float delay;

    public Play(float x, float y, float delay) {
        this.x = x;
        this.y = y;
        this.delay = delay;
    }

     Play(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
        delay = in.readFloat();
    }

    public static final Creator<Play> CREATOR = new Creator<Play>() {
        @Override
        public Play createFromParcel(Parcel in) {
            return new Play(in);
        }

        @Override
        public Play[] newArray(int size) {
            return new Play[size];
        }
    };

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float delay() {
        return delay;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Play that = (Play) obj;
        return this.x == that.x &&
                this.y == that.y &&
                this.delay == that.delay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, delay);
    }

    @Override
    public String toString() {
        return "Play[" +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "delay=" + delay + ']';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeFloat(delay);
    }
}
