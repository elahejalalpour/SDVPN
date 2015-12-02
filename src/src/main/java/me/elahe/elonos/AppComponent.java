/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.elahe.elonos;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.app.ApplicationService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.PointToPointIntent;
import org.onosproject.sdnip.IntentSynchronizer;
import org.onosproject.ui.table.cell.AppIdFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private HostService hostService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected IntentService intentService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected ApplicationService applicationService;

	@Activate
	protected void activate() {
		ApplicationId appId = applicationService.getId("me.elahe.elonos");

		IntentSynchronizer intentSynchronizer = new IntentSynchronizer(appId, intentService);
		intentSynchronizer.start();

		hostService.addListener(new HostHandler(intentSynchronizer, appId));

		applicationService.registerDeactivateHook(appId, intentSynchronizer::stop);

		log.info("Started");
	}

	@Deactivate
	protected void deactivate() {
		log.info("Stopped");
	}

}