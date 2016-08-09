/* *********************************************************************** *
 * project: org.matsim.*
 * Segment.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.cacrowd.casim.matsimconnector.visualizer.cgal;

public class LineSegment {
	public double  x0;
	public double  x1;
	public double  y0;
	public double  y1;
	public double dx;//normalized!!
	public double dy;//normalized!!

	public boolean equalInverse(LineSegment other) {
		return Math.abs(this.x0 - other.x1) < CGAL.EPSILON && Math.abs(this.x1 - other.x0) < CGAL.EPSILON && Math.abs(this.y0 - other.y1) < CGAL.EPSILON && Math.abs(this.y1 - other.y0) < CGAL.EPSILON;
	}
	
	@Override
	public String toString() {
		return this.x0+":"+this.y0 +"  " +this.x1+":"+this.y1;
	}
}