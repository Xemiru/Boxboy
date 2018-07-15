/*
 * MIT License
 *
 * Copyright (c) 2018 Tellerva, Marc Lawrence
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.xemiru.sponge.boxboy.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for creating animations using some kind of object.
 *
 * <p>Animation timings using this class are per-instance and begin on the first call of {@link #getCurrentFrame()}.</p>
 *
 * @param <T> the type of the object representing the animation's frames
 */
public class Animation<T> {

    /**
     * Internal data class for storing frame data.
     */
    private class Frame {

        private T stack;
        private long time;

    }

    private long max;
    private long start;
    private List<Frame> frames;
    private Frame currentFrame;

    public Animation() {
        this.max = 0;
        this.start = -1;
        this.frames = new ArrayList<>();
        this.currentFrame = null;
    }

    /**
     * Returns whether or not this {@link Animation} has no frames.
     *
     * @return if this Animation has no frames
     */
    public boolean isEmpty() {
        return this.frames.isEmpty();
    }

    /**
     * Adds a new frame to this {@link Animation}.
     *
     * <p>The time specified is in milliseconds.</p>
     *
     * <p>Minecraft's update rate of 20 ticks per second sets the lowest effective value for this method to be 50ms
     * (1/20th of a second).</p>
     *
     * @param obj the object representing the frame and thus the object returned upon calling {@link #getCurrentFrame()}
     * @param time how long the frame is displayed, in milliseconds
     * @return this Animation, for chaining
     */
    public Animation<T> frame(T obj, long time) {
        Frame frame = new Frame();
        frame.stack = obj;
        frame.time = time;

        frames.add(frame);
        this.max += time;
        return this;
    }

    /**
     * Returns whether or not the frame being currently returned by {@link #getCurrentFrame()} is different from the one
     * it previously gave.
     *
     * @return whether or not {@link #getCurrentFrame()} returns a new frame
     */
    public boolean isNewFrame() {
        return this.currentFrame != this.findCurrent();
    }

    /**
     * Returns the object representing the current frame of this {@link Animation}.
     *
     * <p>If there are no frames registered with this helper, an {@link IllegalStateException} is thrown stating so.</p>
     *
     * <p>If there is only one frame registered with this helper, that one frame's object is always returned.</p>
     *
     * <p>Otherwise, the animation timer is started (if it hasn't been yet), and the frame is retrieved based on the
     * time passed since then.</p>
     *
     * @return the object of the current frame
     */
    public T getCurrentFrame() {
        this.currentFrame = this.findCurrent();
        return this.currentFrame.stack;
    }

    /**
     * Returns a new {@link Animation} holding a copy of this one's frames.
     *
     * @return a copy of this Animation
     */
    public Animation<T> clone() {
        Animation<T> anim = new Animation<>();
        anim.max = this.max;
        anim.frames.addAll(this.frames);

        return anim;
    }

    /**
     * Internal method.
     *
     * <p>Calculates the current frame.</p>
     *
     * @return the current frame
     */
    private Frame findCurrent() {
        if (this.frames.size() < 1) throw new IllegalStateException("No frames registered in animation");
        if (this.frames.size() == 1) return this.frames.get(0);

        if (this.start < 0) this.start = System.currentTimeMillis();
        long diff = (System.currentTimeMillis() - this.start) % max;

        long time = 0;
        for (Frame frame : this.frames) {
            time += frame.time;
            if (diff <= time) return frame;
        }

        return this.frames.get(this.frames.size() - 1);
    }

}
