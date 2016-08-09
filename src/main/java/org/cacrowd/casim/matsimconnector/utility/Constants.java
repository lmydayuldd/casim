package org.cacrowd.casim.matsimconnector.utility;

import java.util.ArrayList;
import java.util.List;

public class Constants {

	public static final String CA_MOBSIM_MODE = "MobsimCA";
	public static final String CA_LINK_MODE = "walkCA";
	public static final String WALK_LINK_MODE = "walk";
	public static final String CAR_LINK_MODE = "car";
	public static final String TO_Q_LINK_MODE = "CA->Q";
	public static final String TO_CA_LINK_MODE = "Q->CA";
	public static final double CA_CELL_SIDE = 0.4;
	public static final double CA_STEP_DURATION = .3;
	public static final double PEDESTRIAN_SPEED = CA_CELL_SIDE / CA_STEP_DURATION;
	/**
	 * name to use to add CAScenario to a matsim scenario as a scenario element
	 **/
	public static final String CASCENARIO_NAME = "CAScenario";
	public static final double TRANSITION_AREA_LENGTH = CA_CELL_SIDE * 5;
	public static final Double TRANSITION_LINK_LENGTH = TRANSITION_AREA_LENGTH / 2.;
	public static final int TRANSITION_AREA_COLUMNS = (int) (TRANSITION_AREA_LENGTH / CA_CELL_SIDE);
	public static final String RESOURCE_PATH = "src/main/resources";
	public static final String COORDINATE_SYSTEM = "EPSG:3395";
	public static String[] ORIGIN_FLOWS = {"n"};    //each char denote one origin of flow (e.g. "e" stays for "east")
	public static boolean stopOnStairs;
	/** this is for the generation of the fundamental diagram of the CA: pedestrian will be kept inside the
	 * CAEnvironment until this time (in seconds). Keep to 0 if you want to run normal simulation.**/
	public static int CA_TEST_END_TIME = 0; //1200;
	public static double SIMULATION_DURATION = 22000;
	public static int SIMULATION_ITERATIONS = 10;
	/**
	 * global density value used to efficiently compute the test of the fundamental diagram.
	 * Used by DensityGrid only if Constants.DENSITY_GRID_RADIUS==0
	 * **/
	public static double GLOBAL_DENSITY;
	public static Double FLOPW_CAP_PER_METER_WIDTH = 1.2;
	public static Double FAKE_LINK_WIDTH = 10.;  // 1.2;
	public static Double CA_LINK_LENGTH = 10.;
	public static boolean MARGINAL_SOCIAL_COST_OPTIMIZATION = false;
	public static String PATH;
	public static final String DEBUG_TEST_PATH = PATH+"/debug";
	public static String FD_TEST_PATH = PATH+"/FD/";
	public static String INPUT_PATH = DEBUG_TEST_PATH+"/input";
	public static String OUTPUT_PATH = DEBUG_TEST_PATH+"/output";
	public static String ENVIRONMENT_FILE = "ABMUS_PG_station_separated.csv";
	public static boolean BRAESS_WL = false;
	public static boolean VIS = true;
	public static boolean SAVE_FRAMES = false;
	public static List<String> stairsLinks;

	static {
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("win") >= 0)
			PATH = "C:/Users/Luca/Documents/uni/Dottorato/Juelich/developing_stuff/Test";
		else
			PATH = "/tmp/TestCA";
	}

	static{
		stairsLinks = new ArrayList<String>();
	}
}