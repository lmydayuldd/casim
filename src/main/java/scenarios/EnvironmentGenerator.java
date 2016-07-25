package scenarios;

import java.util.ArrayList;
import java.util.List;

import pedca.environment.grid.EnvironmentGrid;
import pedca.environment.grid.GridPoint;
import pedca.environment.grid.neighbourhood.Neighbourhood;
import pedca.environment.markers.Destination;
import pedca.environment.markers.FinalDestination;
import pedca.environment.markers.MarkerConfiguration;
import pedca.environment.markers.Start;
import pedca.environment.markers.TacticalDestination;
import pedca.environment.network.Coordinate;
import pedca.utility.Constants;
import pedca.utility.NeighbourhoodUtility;

public class EnvironmentGenerator {

	public static void initCorridor(EnvironmentGrid environment){
		for (int row = 0; row<environment.getRows(); row++)
			for(int col = 0; col<environment.getColumns();col++)
				environment.setCellValue(row, col, Constants.ENV_WALKABLE_CELL);
	}
	
	public static void initBottleneckScenario(EnvironmentGrid environment, float bottleneckWidth, float bottleneckHeight, float bottleneckPosY){
		initCorridorWithWalls(environment, true);
		int b_centerY = (int)(bottleneckPosY/Constants.CELL_SIZE);
		int b_centerX = environment.getColumns()/2;
		int discreteWidth = (int)Math.round(bottleneckWidth/Constants.CELL_SIZE);
		int discreteHeigth = (int)Math.round(bottleneckHeight/Constants.CELL_SIZE);
		
		//borders of the two wall
		int wallTop = (int) (b_centerY + Math.floor((discreteHeigth-1)/2));
		int wallBottom = wallTop - discreteHeigth + 1;
		
		int wallEast = (int)(b_centerX + Math.floor((discreteWidth-1)/2)) + 1;
		int wallWest = wallEast - discreteWidth - 1;
		
		for (int y = wallBottom; y<=wallTop;y++){
			for(int x = 0;x<=wallWest;x++){
				environment.setCellValue(y, x, Constants.ENV_OBSTACLE);
			}
			for(int x=wallEast;x<environment.getColumns();x++){
				environment.setCellValue(y, x, Constants.ENV_OBSTACLE);
			}
		}
		
		//tactical destination related to the bottleneck
		for(int x = wallWest+1;x<wallEast;x++){
			environment.setCellValue(wallBottom, x, Constants.ENV_TACTICAL_DESTINATION);
		}
		
		
	}	
	
	public static void initCorridorWithWalls(EnvironmentGrid environment, boolean rotate90Degrees){
		for (int row = 0; row<environment.getRows(); row++){
			for(int col = 0; col<environment.getColumns();col++){
				if (rotate90Degrees){
					if ((col==0||col==environment.getColumns()-1))
						environment.setCellValue(row, col, Constants.ENV_OBSTACLE);
					else
						environment.setCellValue(row, col, Constants.ENV_WALKABLE_CELL);
				}else{
					if ((row==0||row==environment.getRows()-1))
						environment.setCellValue(row, col, Constants.ENV_OBSTACLE);
					else
						environment.setCellValue(row, col, Constants.ENV_WALKABLE_CELL);
				}
			}
		}
	}
	
	public static Destination getCorridorEastDestination(EnvironmentGrid environment){
		ArrayList <GridPoint>cells = generateColumn(new GridPoint(environment.getColumns()-1,0),new GridPoint(environment.getColumns()-1,environment.getRows()-1));
		GridPoint environmentCenter = new GridPoint(environment.getColumns()/2,environment.getRows()/2);
		return new FinalDestination(generateCoordinates(cells),cells, environmentCenter);
	}

	public static Destination getCorridorWestDestination(EnvironmentGrid environment){
		ArrayList <GridPoint>cells = generateColumn(new GridPoint(0,0),new GridPoint(0,environment.getRows()-1));
		GridPoint environmentCenter = new GridPoint(environment.getColumns()/2,environment.getRows()/2);
		return new FinalDestination(generateCoordinates(cells),cells, environmentCenter);
	}
	
	public static Start getCorridorEastStart(EnvironmentGrid environment){
		ArrayList <GridPoint>cells = generateColumn(new GridPoint(environment.getColumns()-1,0),new GridPoint(environment.getColumns()-1,environment.getRows()-1));
		return new Start(cells);
	}
	
	public static Start getCorridorWestStart(EnvironmentGrid environment){
		ArrayList <GridPoint>cells = generateColumn(new GridPoint(0,0),new GridPoint(0,environment.getRows()-1));
		return new Start(cells);
	}
	
	public static ArrayList<GridPoint> generateColumn(GridPoint start, GridPoint end){
		ArrayList<GridPoint> result = new ArrayList<GridPoint>();
		for(int i = start.getY(); i<=end.getY();i++)
			result.add(new GridPoint(start.getX(),i));
		return result;
	}
	
	public static ArrayList<GridPoint> generateRow(GridPoint start, GridPoint end){
		ArrayList<GridPoint> result = new ArrayList<GridPoint>();
		for(int j = start.getX(); j<=end.getX();j++)
			result.add(new GridPoint(j,start.getY()));
		return result;
	}

	public static Coordinate generateCoordinates(ArrayList<GridPoint> cells) {
		return generateCoordinates(cells, new GridPoint(0,0));
	}

	public static Coordinate generateCoordinates(ArrayList<GridPoint> cells, GridPoint shift) {
		Coordinate result = calculateCentroid(cells);
		result.setX(result.getX()+shift.getX());
		result.setY(result.getY()+shift.getY());
		return result;
	}

	public static Coordinate calculateCentroid(ArrayList<GridPoint> cells) {
		Coordinate result = new Coordinate(0, 0);
		for (GridPoint point : cells){
			result.setX(result.getX()+(point.getX()*Constants.CELL_SIZE)+Constants.CELL_SIZE/2);
			result.setY(result.getY()+(point.getY()*Constants.CELL_SIZE)+Constants.CELL_SIZE/2);
		}
		result.setX(result.getX()/cells.size());
		result.setY(result.getY()/cells.size());
		return result;
	}

	public static MarkerConfiguration searchFinalDestinations(EnvironmentGrid environmentGrid) {
		MarkerConfiguration markerConfiguration = new MarkerConfiguration();
		GridPoint environmentCenter = new GridPoint(environmentGrid.getColumns()/2,environmentGrid.getRows()/2);
		boolean found = false;
		ArrayList<GridPoint> cells = null;
		for (int i=0;i<environmentGrid.getRows();i+=1){
			for (int j=0;j<environmentGrid.getColumns();j+=1){
				GridPoint cell = new GridPoint(j, i);
				if(environmentGrid.belongsToFinalDestination(cell) && !found){
					found = true;
					cells = new ArrayList<GridPoint>();
					cells.add(cell);
				}else if(environmentGrid.belongsToFinalDestination(cell) && found){
					cells.add(cell);
				}else if (found){
					//skip vertical markers or any other FinalDestination marker drawn in only 1 cell
					if (cells.size()>1)
						markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
					found = false;
				}
			}
			if (found){
				//skip vertical markers or any other FinalDestination marker drawn in only 1 cell
				if (cells.size()>1) 
					markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
				found = false;
			}
		}
		cells = null;
		for (int j=0;j<environmentGrid.getColumns();j+=1){
			for (int i=0;i<environmentGrid.getRows();i+=1){
				GridPoint cell = new GridPoint(j, i);
				if(environmentGrid.belongsToFinalDestination(cell) && !found){
					found = true;
					cells = new ArrayList<GridPoint>();
					cells.add(cell);
				}else if(environmentGrid.belongsToFinalDestination(cell) && found){
					cells.add(cell);
				}else if (found){
					//skip horizontal markers or any other FinalDestination marker drawn in only 1 cell
					if (cells.size()>1)
						markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
					found = false;
				}
			}
			if (found){
				//skip horizontal markers or any other FinalDestination marker drawn in only 1 cell
				if (cells.size()>1)
					markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
				found = false;
			}
		}
		return markerConfiguration;
	}
	
	public static MarkerConfiguration generateBorderDestinations(EnvironmentGrid environmentGrid) {
		MarkerConfiguration markerConfiguration = new MarkerConfiguration();
		GridPoint environmentCenter = new GridPoint(environmentGrid.getColumns()/2,environmentGrid.getRows()/2);
		boolean found = false;
		ArrayList<GridPoint> cells = null;
		for (int i=0;i<environmentGrid.getRows();i+=environmentGrid.getRows()-1){
			for (int j=0;j<environmentGrid.getColumns();j++){
				GridPoint cell = new GridPoint(j, i);
				if(environmentGrid.belongsToExit(cell) && !found){
					found = true;
					cells = new ArrayList<GridPoint>();
					cells.add(cell);
				}else if(environmentGrid.belongsToExit(cell) && found){
					cells.add(cell);
				}else if (found){
					if (j>1) //skip corner
						markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
					found = false;
				}
			}
			if (found){
				if (cells.size()>1) //skip corner for the moment
					markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
				found = false;
			}
		}
		cells = null;
		for (int j=0;j<environmentGrid.getColumns();j+=environmentGrid.getColumns()-1){
			for (int i=0;i<environmentGrid.getRows();i++){
				GridPoint cell = new GridPoint(j, i);
				if(environmentGrid.belongsToExit(cell) && !found){
					found = true;
					cells = new ArrayList<GridPoint>();
					cells.add(cell);
				}else if(environmentGrid.belongsToExit(cell) && found){
					cells.add(cell);
				}else if (found){
					if (i==1){ //add corner if it has not been added before
						if	(!markerConfiguration.getDestinations().get(markerConfiguration.getDestinations().size()-1).getCells().contains(cells.get(0)))
							markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
					}else
						markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
					found = false;
				}
			}
			if (found){
				markerConfiguration.addDestination(new FinalDestination(generateCoordinates(cells), cells, environmentCenter));
				found = false;
			}
		}
		return markerConfiguration;
	}
	
	public static void addTacticalDestinations(MarkerConfiguration markerConfiguration, EnvironmentGrid environmentGrid){
		ArrayList<GridPoint> consideredCells = new ArrayList<GridPoint>();
		ArrayList<GridPoint> destinationCells = null;
		for (int i=0;i<environmentGrid.getRows();i++){
			for (int j=0;j<environmentGrid.getColumns();j++){
				GridPoint cell = new GridPoint(j, i);
				if (environmentGrid.belongsToTacticalDestination(cell) && !consideredCells.contains(cell)){
					destinationCells = new ArrayList<GridPoint>();
					List<GridPoint> visitList = new ArrayList<GridPoint>();
					visitList.add(cell);
					while(!visitList.isEmpty()){
						GridPoint currentCell = visitList.get(0);
						visitList.remove(0);
 						destinationCells.add(currentCell);
						consideredCells.add(currentCell);
						Neighbourhood neighbourhood = NeighbourhoodUtility.calculateVonNeumannNeighbourhood(currentCell);
						for (GridPoint neighbour : neighbourhood.getObjects()){
							if (!destinationCells.contains(neighbour) && environmentGrid.belongsToTacticalDestination(neighbour)){
								visitList.add(neighbour);
							}
						}
					
					}
					
					/**
					GridPoint difference = null;
					for (GridPoint neighbour : neighbourhood.getObjects()){
						if (!neighbour.equals(cell) && environmentGrid.belongsToTacticalDestination(neighbour)){
							difference = MathUtility.gridPointDifference(cell, neighbour);
							break;
						}
					}
					if (difference != null){
						GridPoint neighbour = MathUtility.gridPointDifference(cell, difference);
						do{
							cell = neighbour;
							cells.add(cell);
							consideredCells.add(cell);
							neighbour = MathUtility.gridPointDifference(cell, difference);
						}while (environmentGrid.belongsToTacticalDestination(neighbour));
					}**/
					
					TacticalDestination tacticalDestination;
					if (environmentGrid.belongsToDelayedDestination(cell))
						tacticalDestination = new TacticalDestination(generateCoordinates(destinationCells), destinationCells, environmentGrid.isStairsBorder(destinationCells.get(0)), 3);
					else
						tacticalDestination = new TacticalDestination(generateCoordinates(destinationCells), destinationCells, environmentGrid.isStairsBorder(destinationCells.get(0)));
					markerConfiguration.addDestination(tacticalDestination);
				}
			}
		}
	}
}
