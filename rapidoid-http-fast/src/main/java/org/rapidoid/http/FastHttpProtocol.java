package org.rapidoid.http;

/*
 * #%L
 * rapidoid-http-fast
 * %%
 * Copyright (C) 2014 - 2016 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.buffer.Buf;
import org.rapidoid.data.Range;
import org.rapidoid.data.Ranges;
import org.rapidoid.http.processor.HttpProcessor;
import org.rapidoid.net.Protocol;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;
import org.rapidoid.wrap.BoolWrap;

@Authors("Nikolche Mihajlovski")
@Since("5.1.0")
public class FastHttpProtocol implements Protocol {

	private static final HttpParser HTTP_PARSER = new HttpParser();

	private final HttpProcessor processor;

	public FastHttpProtocol(HttpProcessor processor) {
		this.processor = processor;
	}

	@SuppressWarnings("unchecked")
	public void process(Channel channel) {
		if (channel.isInitial()) {
			return;
		}

		Buf buf = channel.input();
		RapidoidHelper helper = channel.helper();

		Range[] ranges = helper.ranges1.ranges;
		Ranges headers = helper.ranges2;

		BoolWrap isGet = helper.booleans[0];
		BoolWrap isKeepAlive = helper.booleans[1];

		Range verb = ranges[ranges.length - 1];
		Range uri = ranges[ranges.length - 2];
		Range path = ranges[ranges.length - 3];
		Range query = ranges[ranges.length - 4];
		Range protocol = ranges[ranges.length - 5];
		Range body = ranges[ranges.length - 6];

		HTTP_PARSER.parse(buf, isGet, isKeepAlive, body, verb, uri, path, query, protocol, headers, helper);

		processor.onRequest(channel, isGet.value, isKeepAlive.value, body, verb, uri, path, query, protocol, headers);
	}

}
