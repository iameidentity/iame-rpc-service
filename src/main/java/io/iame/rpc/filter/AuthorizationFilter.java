/*
  MIT License
  Copyright (c) 2018 IAME Ltd
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
 */

package io.iame.rpc.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.iame.rpc.persistence.entity.ApiKey.Status;
import io.iame.rpc.persistence.repository.ApiKeyRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

public class AuthorizationFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

	@Autowired
	private ApiKeyRepository apiKeyRepository;

	@Value("${qtumnode.username}")
	protected String qtumnodeUsername;

	@Value("${qtumnode.password}")
	protected String qtumnodePassword;

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 6;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();

		String apiKey = request.getParameter("apiKey");

		if (apiKey == null || apiKey.trim().isEmpty()) {
          log.info("Invalid apiKey {}", apiKey);
			ctx.setResponseStatusCode(401);
			ctx.setSendZuulResponse(false);
		} else {
			// Authenticate API Key
			if (!apiKeyRepository.existsByApiKeyAndStatus(apiKey, Status.active)) {
              log.info("Invalid apiKey {}", apiKey);
				ctx.setResponseStatusCode(401);
				ctx.setSendZuulResponse(false);
			} else {
				// Remove apiKey from request params
				Map<String, List<String>> params = ctx.getRequestQueryParams();
				params.remove("apiKey");
				ctx.setRequestQueryParams(params);

				String prefix = ctx.getZuulRequestHeaders().get(FilterConstants.X_FORWARDED_PREFIX_HEADER);
				if ("/qtum".equals(prefix)) {
					// Add authorization header for Qtum Node
					ctx.addZuulRequestHeader("Authorization", "Basic " + Base64.getEncoder()
                            .encodeToString((qtumnodeUsername + ":" + qtumnodePassword).getBytes(StandardCharsets.UTF_8)));
				}
			}
		}

		return null;
	}
}
