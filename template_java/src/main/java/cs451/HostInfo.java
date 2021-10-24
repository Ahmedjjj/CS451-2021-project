package cs451;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HostInfo {
	private static Host hostMap[];
	private static int currentHostId;
	private static int numHosts;
	private static Map<Integer, Map<String, Integer>> portAndIpToId;

	public static void configureFromHostList(List<Host> hostList) {
		numHosts = Collections.max(hostList).getId();
		hostMap = new Host[numHosts];
		portAndIpToId = new HashMap<Integer, Map<String, Integer>>();

		hostList.forEach(host -> {
			hostMap[host.getId() - 1] = host;
			portAndIpToId.computeIfAbsent(host.getPort(), port -> new HashMap<String, Integer>()).put(host.getIp(),
					host.getId());
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

	public static int hostIdfromIpAndPort(String ip, int port) {
		System.out.println(portAndIpToId);
		return portAndIpToId.get(port).get(ip);
	}

}
