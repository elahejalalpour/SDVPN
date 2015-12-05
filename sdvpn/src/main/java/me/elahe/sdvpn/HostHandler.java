package me.elahe.sdvpn;

import java.util.HashSet;
import java.util.Set;

import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.SinglePointToMultiPointIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostHandler implements HostListener {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private IntentService intentService;
	private HostService hostService;
	private ApplicationId appId;

	public HostHandler(IntentService intentService, ApplicationId appId, HostService hostService) {
		this.intentService = intentService;
		this.appId = appId;
		this.hostService = hostService;
	}

	@Override
	public void event(HostEvent ev) {
		if (ev.type() == HostEvent.Type.HOST_ADDED) {

			
			
			for (Host host : hostService.getHosts()) {
				SinglePointToMultiPointIntent broad = broadcastBuilder(host);
				
				if (host.id().equals(ev.subject().id()))
					continue;

				log.info("************************");
				log.info(host.toString());
				log.info(ev.subject().toString());
				log.info("************************");

				HostToHostIntent h = tunnelBuilder(host, ev.subject());
				
				intentService.submit(h);
				intentService.submit(broad);
			}
			SinglePointToMultiPointIntent broad = broadcastBuilder(ev.subject());
			intentService.submit(broad);
		}
	}

	private SinglePointToMultiPointIntent broadcastBuilder(Host src) {
		TrafficTreatment treatment = DefaultTrafficTreatment.emptyTreatment();
		TrafficSelector selector = DefaultTrafficSelector.builder()
				.matchEthDst(MacAddress.BROADCAST)
				.build();
		
        String strKey = "broad-" + src.id();
        Key key = Key.of(strKey, appId);
        
        if (intentService.getIntent(key) != null)
        	intentService.purge(intentService.getIntent(key));
		
		Set<ConnectPoint> dsts = new HashSet<>();
		for (Host dst : hostService.getHosts()) {
			if (dst.id().equals(src.id()))
				continue;
			dsts.add(dst.location());
		}
		
		return SinglePointToMultiPointIntent.builder()
				.appId(appId)
				.key(key)
				.ingressPoint(src.location())
				.egressPoints(dsts)
				.selector(selector)
				.treatment(treatment)
				.build();
	}
	
	private HostToHostIntent tunnelBuilder(Host src, Host dst) {
        HostId srcId = src.id();
        HostId dstId = dst.id();
        
        String strKey = "uni-" + src.id() + "-->" + dst.id();
        Key key = Key.of(strKey, appId);
        
		TrafficSelector selector = DefaultTrafficSelector.emptySelector();
		
        TrafficTreatment treatment = DefaultTrafficTreatment.emptyTreatment();
        return HostToHostIntent.builder()
        		.appId(appId)
                .key(key)
                .one(srcId)
                .two(dstId)
                .selector(selector)
                .treatment(treatment)
                .build();
	}

}