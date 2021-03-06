/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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

package org.cacrowd.casim.matsimintegration.hybridsim.run;

import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.matsimintegration.hybridsim.mscb.MSCBCongestionObserver;
import org.cacrowd.casim.matsimintegration.hybridsim.mscb.MSCBTravelDisutility;
import org.cacrowd.casim.matsimintegration.hybridsim.mscb.MSCBTravelDisutilityFactory;
import org.cacrowd.casim.matsimintegration.hybridsim.simulation.HybridMobsimProvider;
import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.cacrowd.casim.matsimintegration.scenarios.DaganzoExperimentRunInfoSender;
import org.cacrowd.casim.matsimintegration.scenarios.DaganzoScenarioGernator;
import org.cacrowd.casim.proto.HybridSimProto;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.HybridNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.IOException;


// Runs the MATSim side of Daganzo MSCB experiment as discussed in Section 4.1 in
// Crociani, L. & Lämmel, G.: Multidestination Pedestrian Flows in Equilibrium: A Cellular Automaton-Based Approach.
// Computer-Aided Civil and Infrastructure Engineering 00 (2016) 1–17
// DOI: 10.1111/mice.12209
public class RunDaganzoMSCBExperiment {

    public static void run(double bottleneckWidth) throws IOException, InterruptedException {

        Config c = ConfigUtils.createConfig();


        final IdIntMapper idIntMapper = new IdIntMapper();
        final Scenario sc = ScenarioUtils.createScenario(c);
        HybridSimProto.Scenario hsc = DaganzoScenarioGernator.generateScenario(sc, idIntMapper, bottleneckWidth);

        GRPCExternalClient client = new GRPCExternalClient("localhost", 9000);
        client.getBlockingStub().initScenario(hsc);

        final Controler controller = new Controler(sc);
        controller.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

        final EventsManager eventsManager = EventsUtils.createEventsManager();

        final MSCBTravelDisutility tc = new MSCBTravelDisutility();
        final MSCBCongestionObserver obs = new MSCBCongestionObserver();


        controller.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {
                addEventHandlerBinding().toInstance(tc);
                addEventHandlerBinding().toInstance(obs);
                addMobsimListenerBinding().toInstance(obs);
                bind(MSCBTravelDisutility.class).toInstance(tc);
                addControlerListenerBinding().toInstance(tc);
                bindCarTravelDisutilityFactory().to(MSCBTravelDisutilityFactory.class);
                bindEventsManager().toInstance(eventsManager);
                addControlerListenerBinding().toProvider(() -> new DaganzoExperimentRunInfoSender(client, bottleneckWidth, "MSCB approach"));
                bind(Mobsim.class).toProvider(HybridMobsimProvider.class);
                bind(HybridNetworkFactory.class).toInstance(new HybridNetworkFactory());
                bind(QNetworkFactory.class).to(HybridNetworkFactory.class);
                bind(IdIntMapper.class).toInstance(idIntMapper);
                bind(GRPCExternalClient.class).toInstance(client);
                bind(Controler.class).toInstance(controller);
            }
        });

        controller.run();
        client.getBlockingStub().shutdown(HybridSimProto.Empty.getDefaultInstance());
        client.shutdown();

    }


}
