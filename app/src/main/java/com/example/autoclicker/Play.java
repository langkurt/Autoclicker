package com.example.autoclicker;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Each play is a coordinate representing where to tap the screen. A delay is used
 * so taps can be chained together and spaced out.
 *
 * */
public class Play implements Parcelable {
    private final int x;
    private final int y;
    private final int delay;

    Play(int x, int y, int delay) {
        this.x = x;
        this.y = y;
        this.delay = delay;
    }

    protected Play(Parcel in) {
        x = in.readInt();
        y = in.readInt();
        delay = in.readInt();
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

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int delay() {
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
        dest.writeInt(x);
        dest.writeInt(y);
        dest.writeInt(delay);
    }
}
