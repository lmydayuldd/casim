/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 */

package org.cacrowd.casim.visualizer;

import processing.core.PApplet;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class FrameSaver {

    private final CyclicBarrier barrier = new CyclicBarrier(2);
    private final String path;
    private final String extension;
    private int frameSkip;
    private int skiped;

    public FrameSaver(String path, String extension, int frameSkip) {
        this.path = path;
        this.extension = extension;
        this.frameSkip = frameSkip;
        this.skiped = frameSkip;
    }

//	public boolean wouldskipNext() {
//		if (this.skiped == this.frameSkip) {
//			return true;
//		}
//		this.skiped++;
//		return false;
//	}


    public void saveFrame(PApplet p, String identifier) {
        if (this.skiped < this.frameSkip) {
            return;
        }
//		this.await();
        this.skiped = 0;
        StringBuffer bf = new StringBuffer();
        bf.append(this.path);
        bf.append("/");
        bf.append(identifier);
        bf.append(".");
        bf.append(this.extension);
        p.saveFrame(bf.toString());
        this.await();
    }

    public boolean incrSkipped() {
        this.skiped++;
        return this.skiped >= this.frameSkip;
    }


    public void await() {
        try {
            this.barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void setSkip(int round) {
        this.frameSkip = round;

    }


}
