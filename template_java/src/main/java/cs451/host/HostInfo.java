package cs451.host;

import java.util.Collections;
import java.util.List;

public final class HostInfo {
	private static Host hostMap[];
	private static int currentHostId;
	private static int numHosts;

	public static void configureFromHostList(List<Host> hostList) {
		numHosts = Collections.max(hostList).getId();
		hostMap = new Host[numHosts];

		hostList.forEach(host -> {
			hostMap[host.getId() - 1] = host;
		});
	}

	public static void setCurrentHostId(int hostId) {
		currentHostId = hostId;
	}

	public static Host getHost(int id) {
		return hostMap[id - 1];
	}

	public static int getCurrentHostId() {
		return currentHostId;
	}

	public static int numHosts() {
		return numHosts;
	}
}
