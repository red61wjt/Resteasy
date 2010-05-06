package org.jboss.resteasy.auth.oauth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth Servlet Filter that interprets OAuth Authentication messages to set the Principal and roles
 * for protected resources. 
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class OAuthFilter implements Filter {

	public static final String OAUTH_AUTH_METHOD = "OAuth";

	private final static Logger logger = LoggerFactory.getLogger(OAuthFilter.class);

	private OAuthProvider provider;
	private OAuthValidator validator;

	public void init(FilterConfig config) throws ServletException {
		logger.info("Loading OAuth Filter");
		ServletContext context = config.getServletContext();
		provider = OAuthUtils.getOAuthProvider(context);
		validator = OAuthUtils.getValidator(context, provider);
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		_doFilter((HttpServletRequest)request, (HttpServletResponse)response, filterChain);
	}
	
	private void _doFilter(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		logger.debug("Filtering "+request.getMethod()+" "+request.getRequestURL().toString());
		OAuthMessage message = OAuthUtils.readMessage(request);
		try{

			message.requireParameters(OAuth.OAUTH_CONSUMER_KEY,
					OAuth.OAUTH_TOKEN,
					OAuth.OAUTH_SIGNATURE_METHOD,
					OAuth.OAUTH_SIGNATURE,
					OAuth.OAUTH_TIMESTAMP,
					OAuth.OAUTH_NONCE);

			logger.debug("Parameters present");
			String consumerKey = message.getParameter(OAuth.OAUTH_CONSUMER_KEY);
			String accessTokenString = message.getParameter(OAuth.OAUTH_TOKEN);
			
			// build some info for verification
			OAuthToken accessToken = provider.getAccessToken(consumerKey, accessTokenString);
			OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, accessToken.getConsumer().getSecret(), null);
			OAuthAccessor accessor = new OAuthAccessor(consumer);
			accessor.accessToken = accessTokenString;
			accessor.tokenSecret = accessToken.getSecret();
			
			// validate the message
			validator.validateMessage(message, accessor, accessToken);
			
			// set the Client's credentials
			final Principal principal = accessToken.getPrincipal();
			final Set<String> roles = accessToken.getRoles();
			HttpServletRequest newRequest = new HttpServletRequestWrapper(request){
				@Override
				public Principal getUserPrincipal(){
					return principal;
				}
				@Override
				public boolean isUserInRole(String role){
					return roles.contains(role);
				}
				@Override
				public String getAuthType(){
					return OAUTH_AUTH_METHOD;
				}
			};
			
			// let the request through with the new credentials
			logger.debug("doFilter");
			filterChain.doFilter(newRequest, response);
		} catch (OAuthException x) {
			OAuthUtils.makeErrorResponse(response, x.getMessage(), x.getHttpCode(), provider);
		} catch (OAuthProblemException x) {
			OAuthUtils.makeErrorResponse(response, x.getProblem(), OAuthUtils.getHttpCode(x), provider);
		} catch (Exception x) {
			logger.error("Exception ", x);
			OAuthUtils.makeErrorResponse(response, x.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR, provider);
		}

	}

}