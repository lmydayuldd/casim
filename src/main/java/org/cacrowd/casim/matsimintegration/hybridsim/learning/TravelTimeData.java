/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016-2017 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 *
 */

package org.cacrowd.casim.matsimintegration.hybridsim.learning;

import org.matsim.api.core.v01.Id;
import org.matsim.vehicles.Vehicle;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class TravelTimeData {
	
	private double minTravelTime;
	private double maxTravelTime;
	private double lastEventTime;
	private Map<Id<Vehicle>,Double> ttAnalysis = new LinkedHashMap<Id<Vehicle>,Double>();
	private Vector<Double> sortedTT = null;
	
	
	public void updateTravelTime(Id<Vehicle> pedId, double time){
		Double entranceTime = ttAnalysis.get(pedId);
		//this means that the agent has just entered the link, or it has left the origin link
		if (entranceTime == null)
			ttAnalysis.put(pedId,time);
		//in this case the travel time can be calculated
		else{
			double newTravelTime = time - entranceTime;
			lastEventTime = time;
			ttAnalysis.put(pedId, newTravelTime);
			if(minTravelTime == 0. || newTravelTime < minTravelTime){
				minTravelTime = newTravelTime;
			}
			if(maxTravelTime == 0. || newTravelTime > maxTravelTime){
				maxTravelTime = newTravelTime;
			}
		}
	}
	
	public double getMinTravelTime(){
		return minTravelTime;
	}
	
	public double getMaxTravelTime(){
		return maxTravelTime;
	}
	
	public double getLastEventTime(){
		return lastEventTime;
	}
	
	/**
	 * LC
	 * @return average of the highest 5% travel times of the link. It overwrites maxTravelTime.
	 */
	public double getAvgMaxTravelTime(){
		if (sortedTT == null && ttAnalysis.values().size() > 10){		//if the size is < 10 then maxTravelTime is already reliable...
			sortedTT = new Vector<Double>(ttAnalysis.values());
			Collections.sort(sortedTT);
			maxTravelTime = sortedTT.get(sortedTT.size()-1);
			int numberEl = (int)(sortedTT.size()*.15);					//15%
			
			for (int i = 1; i<numberEl;i++){
				maxTravelTime += sortedTT.get(sortedTT.size()-i-1);
			}
			maxTravelTime /= numberEl;
		}
		return maxTravelTime;
	}

	public int getNRTravelers() {
		return ttAnalysis.size();
	}
	
}
