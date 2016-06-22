package matsimconnector.engine;

import matsimconnector.agents.Pedestrian;
import matsimconnector.environment.TransitionArea;
import matsimconnector.events.*;
import matsimconnector.utility.Constants;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.qsim.qnetsimengine.CALink;
import org.matsim.core.mobsim.qsim.qnetsimengine.CAQLink;
import pedca.context.Context;
import pedca.engine.AgentMover;
import pedca.environment.grid.GridPoint;

public class CAAgentMover extends AgentMover {

	private CAEngine engineCA;
	private EventsManager eventManager;
//	private boolean stairs = true;

	public CAAgentMover(CAEngine engineCA, Context context, EventsManager eventManager) {
		super(context);
		this.eventManager = eventManager;
		this.engineCA = engineCA;
		Constants.stopOnStairs = false;
	}

	public void step(double now){
		Constants.stopOnStairs = !Constants.stopOnStairs;
//		stairs = !stairs;
		for(int index=0; index<getPopulation().size(); index++){
			Pedestrian pedestrian = (Pedestrian)getPopulation().getPedestrian(index);
			if (pedestrian.isArrived()){
				//Log.log(pedestrian.toString()+" Exited.");
				delete(pedestrian);
				index--;
			} 
			else{
				GridPoint oldPosition = pedestrian.getRealPosition();
//				if (stairs && isOnStairs(pedestrian)){
//					eventManager.processEvent(new CAAgentMoveEvent(now, pedestrian, oldPosition, oldPosition));
//					continue;
//				}				
				GridPoint newPosition = pedestrian.getRealNewPosition();
				moveAgent(pedestrian, now);
				if (Constants.VIS)
					eventManager.processEvent(new CAAgentMoveEvent(now, pedestrian, oldPosition, newPosition));
				if(!pedestrian.isWaitingToSwap() && pedestrian.isEnteringEnvironment()){
					moveToCA(pedestrian, now);
				}else if (!pedestrian.isWaitingToSwap()&& pedestrian.isFinalDestinationReached() && !pedestrian.hasLeftEnvironment()){
					Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
					if (engineCA.getCALink(nextLinkId) != null){
						changeLinkInsideEnvironment(pedestrian, now);
					}
					else if(now>= Constants.CA_TEST_END_TIME){
						moveToQ(pedestrian, now);
					}
				}	
			}
		}
	}

	public void moveAgent(Pedestrian pedestrian, double now) {
		Double pedestrianTravelTime = pedestrian.lastTimeCheckAtExit;
		pedestrian.move(now);
		if (pedestrianTravelTime != null && pedestrian.lastTimeCheckAtExit != pedestrianTravelTime){
			pedestrianTravelTime = pedestrian.lastTimeCheckAtExit - pedestrianTravelTime;
			eventManager.processEvent(new CAAgentMoveToOrigin(now, pedestrian, pedestrianTravelTime));
		}
	}
	
	private void delete(Pedestrian pedestrian) {
		pedestrian.moveToUniverse();
		getPopulation().remove(pedestrian);
	}

	private void moveToCA(Pedestrian pedestrian, double time) {
		//Log.log(pedestrian.toString() + " Moving inside Pedestrian Grid");
		Id<Link> currentLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId();
		Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
		engineCA.getQCALink(currentLinkId).notifyMoveOverBorderNode(pedestrian.getVehicle(), nextLinkId);
		pedestrian.getVehicle().getDriver().notifyMoveOverNode(nextLinkId);
		
		eventManager.processEvent(new CAAgentEnterEnvironmentEvent(time, pedestrian));
		
		pedestrian.moveToEnvironment();
	}

	private void moveToQ(Pedestrian pedestrian, double time) {
		//Log.log(pedestrian.toString()+" Moving to CAQLink.");
		Id<Link> currentLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId();
		Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
		CAQLink lowResLink = engineCA.getCAQLink(nextLinkId);
		lowResLink.notifyMoveOverBorderNode(pedestrian.getVehicle(), currentLinkId);
		pedestrian.getVehicle().getDriver().notifyMoveOverNode(nextLinkId);
		lowResLink.addFromUpstream(pedestrian.getVehicle());
		
		eventManager.processEvent(new CAAgentLeaveEnvironmentEvent(time, pedestrian));
		
		TransitionArea transitionArea = lowResLink.getTransitionArea();
		pedestrian.moveToTransitionArea(transitionArea);
	}	
	
	private void changeLinkInsideEnvironment(Pedestrian pedestrian, double time) {
		//Log.log(pedestrian.toString()+" changing CALink.");
		Id<Link> currentLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId();
		Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
		CALink nextLinkCA = engineCA.getCALink(nextLinkId);
		nextLinkCA.notifyMoveOverBorderNode(pedestrian.getVehicle(), currentLinkId);
		pedestrian.getVehicle().getDriver().notifyMoveOverNode(nextLinkId);
			
		eventManager.processEvent(new CAAgentChangeLinkEvent(time, pedestrian, currentLinkId.toString(), nextLinkId.toString()));
		
		//TODO CHANGE THE COLOR OF THE AGENT LC
		//eventManager.processEvent(new CAAgentLeaveEnvironmentEvent(time, pedestrian));
		
		//TODO CHANGE THE DESTINATION OF THE AGENT
		pedestrian.refreshDestination();
	}	
	
//	private boolean isOnStairs(Pedestrian pedestrian){
//		try{
//			Id<Link> currentLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId();
//			if (currentLinkId != null){
//				String linkId = currentLinkId.toString();
//				for (String stairId : Constants.stairsLinks){
//					if (stairId.equals(linkId))
//						return true;
//				}
//			}
//			return false;
//		}catch(NullPointerException e){
//			return false;
//		}		
//	}
}