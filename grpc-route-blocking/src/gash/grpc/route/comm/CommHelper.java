package gash.grpc.route.comm;

/**
 * copyright 2021, gash
 *
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTE: as a singleton, the application must configure prior to first call
 */
public class CommHelper {
	protected static Logger logger = LoggerFactory.getLogger("server");
	protected static AtomicReference<CommHelper> instance = new AtomicReference<CommHelper>();
	protected static Properties conf;
	protected Long serverID;
	protected Integer serverPort;
	protected Long nextMessageID;

	private CommHelper() {
		init();
	};

	public static void configure(Properties conf) {
		CommHelper.conf = conf;
	}

	public static CommHelper getInstance() {
		instance.compareAndSet(null, new CommHelper());
		return instance.get();
	}

	private void init() {
		if (conf == null)
			throw new RuntimeException("server not configured!");

		String tmp = conf.getProperty("server.id");
		if (tmp == null)
			throw new RuntimeException("missing server ID");
		serverID = Long.parseLong(tmp);

		tmp = conf.getProperty("server.port");
		if (tmp == null)
			throw new RuntimeException("missing server port");
		serverPort = Integer.parseInt(tmp);
		if (serverPort <= 1024)
			throw new RuntimeException("server port must be above 1024");

		nextMessageID = 0L;
	}

	public static Properties getConf() {
		return conf;
	}

	public Long getServerID() {
		return serverID;
	}

	public synchronized Long getNextMessageID() {
		return ++nextMessageID;
	}

	public Integer getServerPort() {
		return serverPort;
	}
}
